package it.matteolobello.lumine.ui.activity

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Environment
import android.os.Handler
import android.os.Vibrator
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.commit451.morphtransitions.FabTransform
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import it.matteolobello.bottomsheetitemslayout.BottomSheetListLayout
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.DefaultPreferenceManager
import it.matteolobello.lumine.data.receiver.ReceiverIntentFilterActions
import it.matteolobello.lumine.data.service.DownloadNotesService
import it.matteolobello.lumine.data.service.UploadNotesService
import it.matteolobello.lumine.extension.*
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.viewpager.HomeViewPagerAdapter
import it.matteolobello.lumine.ui.fragment.home.NotesFragment
import it.matteolobello.lumine.ui.view.dialog.GenericDialogView
import it.matteolobello.lumine.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.appbar_home.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.abs

class HomeActivity : BaseActivity() {

    val viewModel = ViewModelProvider.NewInstanceFactory().create(HomeViewModel::class.java)

    val appBarLayout: AppBarLayout by lazy { appBar }
    val statusBarShadowView: View by lazy { statusBarElevationView }

    private val colorAccent: Int by lazy {
        fetchAccentColor()
    }

    private val homeViewPagerAdapter: HomeViewPagerAdapter by lazy { viewPager.adapter as HomeViewPagerAdapter }

    private val bottomSheetMenuItems by lazy {
        arrayListOf(
                BottomSheetListLayout.BottomSheetMenuItem(R.drawable.twotone_cloud_download_24px, getString(R.string.download_from_cloud)),
                BottomSheetListLayout.BottomSheetMenuItem(R.drawable.twotone_cloud_upload_24px, getString(R.string.upload_to_cloud)),
                BottomSheetListLayout.BottomSheetMenuItem(R.drawable.twotone_settings_24px, getString(R.string.settings))
        )
    }

    private val notesDataChangeReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            log("Receiver -> ACTION_LOCAL_DATA_CHANGE")

            viewModel.loadUserNotes(context)
        }
    }

    override fun layoutRes() = R.layout.activity_home

    override fun setupUi() {
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))
        setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))

        setupAppBar()
        setupMorphingFab()
        setupViewPager()
        setupProfilePic()
        setupBottomNavigationView()
        setupMenuBottomSheet()

        viewModel.updateLocalNotesWithoutReloading(intent.getParcelableArrayListExtra(BundleKeys.EXTRA_NOTES))
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notesDataChangeReceiver, IntentFilter(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notesDataChangeReceiver)

        super.onDestroy()
    }

    override fun onBackPressed() {
        val dialog = GenericDialogView.findDialogInActivity(this)
        when {
            dialog != null -> dialog.dismiss()
            BottomSheetBehavior.from(menuBottomSheetContainer).state == BottomSheetBehavior.STATE_EXPANDED -> hideBottomSheetMenu()
            else -> super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SettingsActivity.REQUEST_CODE -> {
                if (data != null) {
                    val themeChanged = data.getBooleanExtra(BundleKeys.EXTRA_THEME_CHANGED, false)
                    if (themeChanged) {
                        finish()
                        startActivity(Intent(this, LaunchScreenActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
            }
        }
    }

    private fun setupAppBar() {
        appBar.bringToFront()

        if (viewPager.currentItem == 3) {
            val notes = viewModel.notes.value
            if (notes != null) {
                if (notes.none { it.isTrashed }) {
                    appBarMoreImageView.visibility = View.GONE
                } else {
                    appBarMoreImageView.visibility = View.VISIBLE
                }
            }
        } else {
            appBarMoreImageView.visibility = View.GONE
        }

        appBarMoreImageView.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.home_more_empty_trash)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_empty_trash -> {
                        LocalNotesManager.get().emptyTrash(this)

                        setupAppBar()
                    }
                }

                return@setOnMenuItemClickListener false
            }
            popupMenu.show()

            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(7)
        }
    }

    private fun setupMorphingFab() {
        with(floatingActionButton.layoutParams as CoordinatorLayout.LayoutParams) {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.bottomNavigationBarHeight) + dpToPx(16f)
            marginEnd = dpToPx(16f)
            floatingActionButton.layoutParams = this
        }

        floatingActionButton.setOnClickListener { _ ->
            when (viewPager.currentItem) {
                0 -> {
                    val intent = Intent(this, ExpandedFabActivity::class.java)
                    FabTransform.addExtras(intent,
                            if (viewPager.currentItem == 0) {
                                ContextCompat.getColor(this, android.R.color.white)
                            } else {
                                colorAccent
                            },
                            R.drawable.twotone_pencil_24px,
                            if (viewPager.currentItem == 0) {
                                colorAccent
                            } else {
                                ContextCompat.getColor(this, android.R.color.white)
                            })
                    val options = ActivityOptions.makeSceneTransitionAnimation(this, floatingActionButton,
                            getString(R.string.morph_transition))
                    startActivity(intent, options.toBundle())
                }
                1 -> {
                    val categories = LinkedHashSet<String>()

                    val tasks = viewModel.tasks.value
                    tasks?.forEach {
                        if (!it.isTrashed) {
                            categories.add(it.category)
                        }
                    }

                    val intent = Intent(this, TaskActivity::class.java)
                            .putExtra(BundleKeys.EXTRA_CATEGORIES, ArrayList<String>(categories))
                    startActivity(intent)
                }
                2 -> {
                    startActivity(Intent(this, ReminderActivity::class.java))
                }
            }
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = HomeViewPagerAdapter(this)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count
    }

    private fun setupProfilePic() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val localCachedProfilePicFile = File(Environment.getExternalStorageDirectory().toString() +
                    File.separator +
                    "." +
                    getString(R.string.app_name) +
                    File.separator +
                    currentUser.uid
            )

            bottomSheetListLayout.profilePicImageView.setBorderColor(
                    ContextCompat.getColor(this, R.color.profilePicBorderColor))

            if (localCachedProfilePicFile.exists()) {
                Glide.with(this)
                        .load(localCachedProfilePicFile)
                        .into(profilePicImageView)

                Glide.with(this)
                        .load(localCachedProfilePicFile)
                        .into(bottomSheetListLayout.profilePicImageView)
            } else {
                Glide.with(this)
                        .load(currentUser.photoUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean) = false

                            override fun onResourceReady(resource: Drawable, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                bottomSheetListLayout.profilePicImageView.setImageDrawable(resource)

                                val destFolderPath = Environment.getExternalStorageDirectory().toString() +
                                        File.separator +
                                        "." +
                                        getString(R.string.app_name)
                                File(destFolderPath).mkdirs()

                                val noMediaFile = File(destFolderPath + File.separator + ".nomedia")
                                noMediaFile.createNewFile()

                                localCachedProfilePicFile.createNewFile()

                                val fileOutputStream = FileOutputStream(localCachedProfilePicFile)
                                resource.toBitmap().compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream)
                                fileOutputStream.close()

                                return false
                            }
                        })
                        .into(profilePicImageView)
            }
        }

        profilePicImageView.setOnClickListener {
            showBottomSheetMenu()
        }
    }

    private fun setupBottomNavigationView() {
        val colors = intArrayOf(
                ContextCompat.getColor(this, R.color.unselectedBottomNavigationMenuItem),
                colorAccent
        )

        val states = arrayOf(
                intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
        )

        bottomNavigation.itemTextColor = ColorStateList(states, colors)
        bottomNavigation.itemIconTintList = ColorStateList(states, colors)
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val oldFabBackgroundColor = if (viewPager.currentItem == 0) {
                ContextCompat.getColor(this, android.R.color.white)
            } else {
                colorAccent
            }

            val oldFabIconColor = if (viewPager.currentItem == 0) {
                colorAccent
            } else {
                ContextCompat.getColor(this, android.R.color.white)
            }

            val newIndex = when (menuItem.itemId) {
                R.id.action_notes -> 0
                R.id.action_tasks -> 1
                R.id.action_reminders -> 2
                R.id.action_trash -> 3
                else -> throw UnsupportedOperationException("Unknown index")
            }

            viewPager.currentItem = newIndex

            setupAppBar()

            val fabScale = if (newIndex == 3) 0f else 1f
            floatingActionButton.animate()
                    .scaleX(fabScale)
                    .scaleY(fabScale)
                    .setDuration(100)
                    .start()

            val rippleColor = if (newIndex == 0) {
                colorAccent
            } else {
                ContextCompat.getColor(this, android.R.color.white)
            }

            floatingActionButton.rippleColor = rippleColor

            val appBarTranslationY =
                    when (newIndex) {
                        // 3 -> -appBar.height.toFloat()
                        0 -> {
                            val notesFragment = homeViewPagerAdapter.getItem(0) as? NotesFragment
                            if (notesFragment != null) {
                                val recyclerView = notesFragment.recyclerView
                                -recyclerView.computeVerticalScrollOffset().toFloat()
                            }

                            0f
                        }
                        else -> 0f
                    }
            appBar.animate()
                    .translationY(appBarTranslationY)
                    .setDuration(100)
                    .start()

            val newTitle = when (newIndex) {
                0 -> getString(R.string.notes)
                1 -> getString(R.string.tasks)
                2 -> getString(R.string.reminders)
                3 -> getString(R.string.trash)
                else -> ""
            }

            if (newTitle != appBarTitleTextView.text) {
                appBarTitleTextView.text = newTitle

                val newFabBackgroundColor = if (newIndex == 0) {
                    ContextCompat.getColor(this, android.R.color.white)
                } else {
                    colorAccent
                }

                val newFabIconColor = if (newIndex == 0) {
                    colorAccent
                } else {
                    ContextCompat.getColor(this, android.R.color.white)
                }

                ValueAnimator().evaluateColors({
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(it)
                }, oldFabBackgroundColor, newFabBackgroundColor, duration = 200)

                ValueAnimator().evaluateColors({
                    floatingActionButton.imageTintList = ColorStateList.valueOf(it)
                }, oldFabIconColor, newFabIconColor, duration = 200)

                val newStatusBarElevationViewAlpha =
                        if (newIndex == 0 && ((homeViewPagerAdapter.getItem(0) as NotesFragment)
                                        .recyclerView.computeVerticalScrollOffset().toFloat() > 0))
                            1f
                        else 0f
                statusBarShadowView.animate()
                        .alpha(newStatusBarElevationViewAlpha)
                        .setDuration(200)
                        .start()
            }

            return@setOnNavigationItemSelectedListener true
        }
    }

    @SuppressWarnings("all")
    private fun setupMenuBottomSheet() {
        val behavior = BottomSheetBehavior.from(menuBottomSheetContainer)

        hideBottomSheetMenu()
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(view: View, slideOffset: Float) {
                if (slideOffset.toString() == "NaN") {
                    return
                }

                val slideOffset = abs(slideOffset)
                val dimColor = ArgbEvaluator()
                        .evaluate((1 - slideOffset), Color.TRANSPARENT,
                                ContextCompat.getColor(applicationContext, R.color.maxBottomSheetDimColor)) as Int

                backgroundDimView.setBackgroundColor(dimColor)
                setStatusBarColor(dimColor)
            }

            override fun onStateChanged(view: View, newState: Int) {
                backgroundDimView.isClickable = newState != BottomSheetBehavior.STATE_HIDDEN

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        setStatusBarColor(Color.WHITE)
                    }
                    else -> {
                        ContextCompat.getColor(applicationContext, R.color.maxBottomSheetDimColor)
                    }
                }
            }
        })

        backgroundDimView.setOnClickListener {
            hideBottomSheetMenu()
        }
        backgroundDimView.isClickable = false

        menuBottomSheetContainer.setOnClickListener {
            // Nothing to do
        }

        bottomSheetMenuItems.forEach {
            bottomSheetListLayout.addItem(it)
        }
        bottomSheetListLayout.apply {
            nameSurname = FirebaseAuth.getInstance().currentUser?.displayName ?: getString(R.string.app_name)
            email = FirebaseAuth.getInstance().currentUser?.email ?: getString(R.string.slogan)
            onBottomItemSelected = {
                hideBottomSheetMenu()

                when (it.title) {
                    getString(R.string.download_from_cloud) -> {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            Toast.makeText(applicationContext, R.string.you_need_to_login_first, Toast.LENGTH_LONG).show()
                        } else {
                            GenericDialogView.create(
                                    activity = this@HomeActivity,
                                    title = getString(R.string.warning),
                                    message = getString(R.string.download_from_cloud_dialog_message),
                                    positiveButtonText = getString(R.string.ok),
                                    negativeButtonText = getString(R.string.dismiss),
                                    onPositiveButtonClickListener = { dialogView ->
                                        startService(Intent(applicationContext, DownloadNotesService::class.java))

                                        try {
                                            val statusBarService = getSystemService("statusbar")
                                            val statusBarManager = Class.forName("android.app.StatusBarManager")
                                            val expandNotificationsPanelMethod = statusBarManager.getMethod("expandNotificationsPanel")
                                            expandNotificationsPanelMethod.invoke(statusBarService)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                        dialogView.dismiss()
                                    },
                                    onNegativeButtonClickListener = {
                                        it.dismiss()

                                        Handler().postDelayed({
                                            setStatusBarColor(Color.WHITE)
                                        }, 250)
                                    }
                            )
                        }
                    }
                    getString(R.string.upload_to_cloud) -> {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            Toast.makeText(applicationContext, R.string.you_need_to_login_first, Toast.LENGTH_LONG).show()
                        } else {
                            startService(Intent(applicationContext, UploadNotesService::class.java))
                        }
                    }
                    getString(R.string.settings) -> {
                        startActivityForResult(Intent(applicationContext, SettingsActivity::class.java), SettingsActivity.REQUEST_CODE)
                    }
                }
            }
        }

        bottomSheetListLayout.bottomSheetButton.apply {
            text = getString(R.string.logout)
            setBackgroundColor(fetchAccentColor().changeAlpha(0.15f))
            rippleColor = ColorStateList.valueOf(fetchAccentColor())
            setTextColor(ColorStateList.valueOf(fetchAccentColor()))
            cornerRadius = dpToPx(16f)
            isAllCaps = false
            setOnClickListener { showLogoutDialog() }
        }
    }

    private fun showBottomSheetMenu() {
        BottomSheetBehavior.from(menuBottomSheetContainer).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomSheetMenu() {
        BottomSheetBehavior.from(menuBottomSheetContainer).state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showLogoutDialog() {
        GenericDialogView.create(
                activity = this@HomeActivity,
                title = getString(R.string.warning),
                message = getString(R.string.logout_message),
                positiveButtonText = getString(R.string.logout),
                negativeButtonText = getString(R.string.dismiss),
                onPositiveButtonClickListener = {
                    DefaultPreferenceManager.getInstance().clearAll(applicationContext)
                    LocalNotesManager.get().clearAll(applicationContext)

                    if (FirebaseAuth.getInstance().currentUser != null) {
                        FirebaseAuth.getInstance().signOut()
                    }

                    finish()
                    startActivity(Intent(applicationContext, LaunchScreenActivity::class.java))
                },
                onNegativeButtonClickListener = {
                    it.dismiss()
                }
        )
    }
}