package it.matteolobello.lumine.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rm.freedrawview.FreeDrawView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.recyclerview.ColorsRecyclerViewAdapter
import it.matteolobello.lumine.ui.adapter.recyclerview.StrokeWidthsRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_drawing.*
import java.io.File
import java.io.FileOutputStream

class DrawingActivity : BaseActivity() {

    companion object {
        const val RC_DRAWING = 300

        private const val SPAN_COUNT = 9
    }

    override fun layoutRes() = R.layout.activity_drawing

    override fun setupUi() {
        setStatusBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))

        drawingBackArrowImageView.setOnClickListener { finish() }

        drawingSlidingUpPanelLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    drawingFab.show()
                } else {
                    drawingFab.hide()
                }
            }
        })

        drawingUndoImageView.setOnClickListener {
            drawView.undoLast()
        }

        drawingRedoImageView.setOnClickListener {
            drawView.redoLast()
        }

        drawingColorsRecyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT, RecyclerView.VERTICAL, false)
        drawingColorsRecyclerView.adapter = ColorsRecyclerViewAdapter(this, {
            drawView.paintColor = it
        })

        drawingStrokeWidthsRecyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT, RecyclerView.VERTICAL, false)
        drawingStrokeWidthsRecyclerView.adapter = StrokeWidthsRecyclerViewAdapter(this, {
            drawView.setPaintWidthPx(it.toFloat())
        })

        drawView.paintAlpha = 220
        drawView.setPaintWidthDp(18f)

        drawingStrokeAlphaSeekBar.progress = 220
        drawingStrokeAlphaSeekBar.max = 255
        drawingStrokeAlphaSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                drawView.paintAlpha = if (progress == 0) 10 else progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        drawingFab.setOnClickListener {
            val destFolderPath = Environment.getExternalStorageDirectory().toString() +
                    File.separator +
                    "." +
                    getString(R.string.app_name)
            File(destFolderPath).mkdirs()

            val noMediaFile = File(destFolderPath + File.separator + ".nomedia")
            noMediaFile.createNewFile()

            val destFile = File(destFolderPath + File.separator + System.currentTimeMillis().toString())
            val fileOutputStream = FileOutputStream(destFile)

            drawView.getDrawScreenshot(object : FreeDrawView.DrawCreatorListener {
                override fun onDrawCreated(bitmap: Bitmap) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    setResult(Activity.RESULT_OK, Intent()
                            .putExtra(BundleKeys.EXTRA_DRAWING_OUTPUT_PATH, destFile.path))
                    finish()
                }

                override fun onDrawCreationError() {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            })
        }    }

    override fun onBackPressed() {
        if (drawingSlidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            drawingSlidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }
}