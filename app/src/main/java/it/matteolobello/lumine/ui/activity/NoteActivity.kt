package it.matteolobello.lumine.ui.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Vibrator
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.data.ocr.OcrAsyncTask
import it.matteolobello.lumine.extension.*
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.recyclerview.ImagesRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_note.*
import java.io.File
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

@SuppressLint("RestrictedApi")
class NoteActivity : BaseActivity(), ColorPickerDialogListener {

    private companion object {
        private const val CHOOSING_IMG_DEFAULT = 0
        private const val CHOOSING_IMG_OCR = 1
    }

    private val originalNote: Note? by lazy {
        intent.getParcelableExtra(BundleKeys.EXTRA_NOTE) as Note?
    }

    private lateinit var newNote: Note

    private val isEditMode: Boolean by lazy {
        originalNote != null
    }

    private var choosingImagesCause = CHOOSING_IMG_DEFAULT

    override fun layoutRes() = R.layout.activity_note

    override fun setupUi() {
        if (originalNote != null) {
            newNote = originalNote!!
        } else {
            newNote = Note()
            newNote.color = ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor).toHex()
        }

        setStatusBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))

        setSupportActionBar(bottomAppBar)

        setupUiByNote(originalNote)

        noteBodyEditText.withBullets = intent.getBooleanExtra(BundleKeys.EXTRA_IS_NOTE_LIST, false)
                || (if (originalNote != null) originalNote!!.type == Note.TYPE_LIST else false)

        if (intent.getBooleanExtra(BundleKeys.EXTRA_ADD_DRAWING, false)) {
            startActivityForResult(Intent(this, DrawingActivity::class.java),
                    DrawingActivity.RC_DRAWING)
        } else if (intent.getBooleanExtra(BundleKeys.EXTRA_ADD_IMAGE, false)) {
            startImagePickerActivity()
        }

        if (!newNote.isTrashed) {
            newNoteMoreImageView.visibility = View.GONE
        } else {
            newNoteMoreImageView.setOnClickListener { view ->
                val popupMenu = PopupMenu(this, view)
                popupMenu.inflate(R.menu.note_more_restore_from_trash)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_restore_from_trash -> {
                            newNote.isTrashed = false
                            saveNote()
                        }
                    }

                    return@setOnMenuItemClickListener false
                }

                popupMenu.show()

                (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(7)
            }
        }

        backArrowImageView.setOnClickListener {
            onBackPressed()
        }

        bottomAppBarFab.bringToFront()
        bottomAppBarFab.setOnClickListener {
            saveNote()
        }

        deleteImageWrapper.setOnClickListener {
            val currentItem = (imagesRecyclerView.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
            newNote.images.removeAt(currentItem)

            if (newNote.images.size == 0) {
                imagesListWrapper.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animator: Animator?) {
                            }

                            override fun onAnimationEnd(animator: Animator?) {
                                imagesListWrapper.visibility = View.GONE
                            }

                            override fun onAnimationCancel(animator: Animator?) {
                            }

                            override fun onAnimationStart(animator: Animator?) {
                            }
                        })
                        .start()
            } else {
                imagesListWrapper.alpha = 1f
                imagesListWrapper.visibility = View.VISIBLE
                imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                imagesRecyclerView.adapter = ImagesRecyclerViewAdapter(newNote.images, null)
                imagesRecyclerView.onFlingListener = null
                PagerSnapHelper().attachToRecyclerView(imagesRecyclerView)
                imagesListIndicator.attach(imagesRecyclerView)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            DrawingActivity.RC_DRAWING -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return
                }

                val path = data.getStringExtra(BundleKeys.EXTRA_DRAWING_OUTPUT_PATH)
                handlePickedImages(arrayListOf<File>().apply { add(File(path)) })
            }
            Config.RC_PICK_IMAGES -> {
                if (resultCode != RESULT_OK || data == null) {
                    return
                }

                val images: ArrayList<Image> = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES)

                if (choosingImagesCause == CHOOSING_IMG_OCR) {
                    if (!images.isEmpty()) {
                        showToast(message = getString(R.string.fetching_text))

                        OcrAsyncTask(WeakReference(this), File(images[0].path)) { imageText ->
                            if (TextUtils.isEmpty(imageText)) {
                                showToast(message = getString(R.string.ocr_error))
                            } else {
                                val newBodyBase = if (noteBodyEditText.text != null
                                        && !noteBodyEditText.text!!.isEmpty()) {
                                    "${noteBodyEditText.text}\n\n\n"
                                } else {
                                    ""
                                }

                                val newBody = "$newBodyBase$imageText"
                                noteBodyEditText.setText(newBody)
                            }
                        }.execute()
                    }
                } else {
                    val imageFiles = arrayListOf<File>()

                    images.forEach {
                        imageFiles.add(File(it.path))
                    }

                    try {
                        handlePickedImages(imageFiles)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        showToast(message = getString(R.string.error_selecting_image))
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.note_bottom_appbar_menu, menu)
        menu.tintAllIcons(ContextCompat.getColor(this, R.color.menuItemColor))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_note_color -> showColorPickerDialog()
            R.id.action_add_images -> startImagePickerActivity()
            R.id.action_ocr -> startImagePickerForOcrActivity()
            R.id.action_drawing -> startDrawingActivity()
            R.id.action_move_to_trash -> moveToTrash()
        }

        return true
    }

    override fun onDialogDismissed(dialogId: Int) {
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        ValueAnimator().evaluateColors({ animatedValueColor ->
            newNoteRootLayout.background = ColorDrawable(animatedValueColor)
            newNoteContentWrapper.background = ColorDrawable(animatedValueColor)

            noteTitleEditText.setHintTextColor(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.newNoteTitleHintColorLightBackground else R.color.newNoteTitleHintColor))
            noteLastEditTextView.setTextColor(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.newNoteLastEditColorLightBackground else R.color.newNoteLastEditColor))
            noteBodyEditText.setHintTextColor(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.newNoteBodyHintColorLightBackground else R.color.newNoteBodyHintColor))

            noteTitleEditText.setTextColor(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.newNoteTitleColorLightBackground else R.color.newNoteTitleColor))
            noteBodyEditText.setTextColor(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.newNoteBodyColorLightBackground else R.color.newNoteBodyColor))

            backArrowImageView.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this,
                    if (animatedValueColor.getBrightness() > 130) R.color.backArrowLightBackground else R.color.backArrowColor), PorterDuff.Mode.SRC_ATOP)

        }, Color.parseColor(newNote.color), color, duration = 250)

        setStatusBarColor(color, customBrightnessThreshold = 130)

        newNote.color = color.toHex()
    }

    private fun setupUiByNote(note: Note?) {
        newNote.color = (if (note == null)
            ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor)
        else Color.parseColor(note.color)).toHex()

        onColorSelected(-1, Color.parseColor(newNote.color))

        if (note != null) {
            noteTitleEditText.setText(note.title)
            noteLastEditTextView.setText(note.lastEditTimeStamp.getRelativeTimeDisplayString(this))
            noteBodyEditText.setText(note.body)

            if (note.images.size > 0) {
                imagesListWrapper.alpha = 1f
                imagesListWrapper.visibility = View.VISIBLE
                imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                imagesRecyclerView.adapter = ImagesRecyclerViewAdapter(note.images, null)
                imagesRecyclerView.onFlingListener = null
                PagerSnapHelper().attachToRecyclerView(imagesRecyclerView)
                imagesListIndicator.attach(imagesRecyclerView)
            }
        }
    }

    private fun saveNote() {
        newNote.id = if (!TextUtils.isEmpty(newNote.id))
            newNote.id
        else
            Note.generateNewId(this)

        newNote.type = if (noteBodyEditText.withBullets) {
            Note.TYPE_LIST
        } else {
            Note.TYPE_PLAIN_TEXT
        }

        val title = noteTitleEditText.text.toString()
        val body = noteBodyEditText.text.toString()

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) {
            showToast(message = getString(R.string.note_error_empty_title_and_body))
            return
        }

        newNote.title = title
        newNote.body = body

        newNote.isMarkedAsDone = false
        newNote.category = ""
        newNote.lastEditTimeStamp = System.currentTimeMillis().toDouble()

        addNewNote(newNote)
        finish()
    }

    private fun addNewNote(newNote: Note) {
        with(LocalNotesManager.get()) {
            if (isEditMode) {
                editNote(applicationContext, originalNote!!, newNote)
            } else {
                insertNewNote(applicationContext, newNote)
            }
        }
    }

    private fun handlePickedImages(nonHiddenFolderFiles: ArrayList<File>) {
        val progressDialog = ProgressDialog(this).apply {
            title = getString(R.string.loading_images)
            max = nonHiddenFolderFiles.size
            progress = 0
            isIndeterminate = false
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
        }
        progressDialog.show()

        thread {
            nonHiddenFolderFiles.forEach {
                val cachedImage = it.toBitmap().saveToCache(this)
                newNote.images.add(cachedImage.path)

                progressDialog.progress = newNote.images.size
            }

            runOnUiThread {
                progressDialog.dismiss()

                imagesListWrapper.alpha = 1f
                imagesListWrapper.visibility = View.VISIBLE
                imagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                imagesRecyclerView.adapter = ImagesRecyclerViewAdapter(newNote.images, null)
                imagesRecyclerView.onFlingListener = null
                PagerSnapHelper().attachToRecyclerView(imagesRecyclerView)
                imagesListIndicator.attach(imagesRecyclerView)
            }
        }
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog.newBuilder()
                .setPresets(resources.getIntArray(R.array.noteColors))
                .setAllowCustom(false)
                .setShowColorShades(false)
                .setColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))
                .show(this)
    }

    private fun startImagePickerActivity() {
        choosingImagesCause = CHOOSING_IMG_DEFAULT

        ImagePicker.with(this)
                .setToolbarColor("#212121")
                .setStatusBarColor("#000000")
                .setToolbarTextColor("#FFFFFF")
                .setToolbarIconColor("#FFFFFF")
                .setProgressBarColor("#4CAF50")
                .setBackgroundColor("#212121")
                .setMultipleMode(true)
                .setShowCamera(true)
                .setDoneTitle(getString(R.string.done))
                .setLimitMessage(getString(R.string.max_num_images_picked_reached))
                .setMaxSize(10)
                .start()
    }

    private fun startImagePickerForOcrActivity() {
        choosingImagesCause = CHOOSING_IMG_OCR

        showToast(message = getString(R.string.ocr_beta), length = Toast.LENGTH_LONG)

        ImagePicker.with(this)
                .setToolbarColor("#212121")
                .setStatusBarColor("#000000")
                .setToolbarTextColor("#FFFFFF")
                .setToolbarIconColor("#FFFFFF")
                .setProgressBarColor("#4CAF50")
                .setBackgroundColor("#212121")
                .setMultipleMode(false)
                .setShowCamera(true)
                .setDoneTitle(getString(R.string.done))
                .setLimitMessage(getString(R.string.max_num_images_picked_reached))
                .start()
    }

    private fun startDrawingActivity() {
        startActivityForResult(Intent(this, DrawingActivity::class.java),
                DrawingActivity.RC_DRAWING)
    }

    private fun moveToTrash() {
        if (!isEditMode) {
            finish()
        } else {
            showToast(message = getString(R.string.moving_to_trash), length = Toast.LENGTH_LONG)

            newNote.isTrashed = true

            LocalNotesManager.get().editNote(this, originalNote!!, newNote)

            finish()
        }
    }
}