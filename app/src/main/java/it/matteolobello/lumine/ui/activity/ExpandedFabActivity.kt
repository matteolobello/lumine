package it.matteolobello.lumine.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.commit451.morphtransitions.FabTransform
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.extension.dpToPx
import it.matteolobello.lumine.extension.setLightNavigationBar
import it.matteolobello.lumine.extension.setLightStatusBar
import kotlinx.android.synthetic.main.activity_expanded_fab.*

// NOTE: do NOT convert this to BaseActivity
class ExpandedFabActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expanded_fab)

        setLightStatusBar()
        setLightNavigationBar()

        with(expandedFabContainer.layoutParams as FrameLayout.LayoutParams) {
            bottomMargin = resources.getDimensionPixelSize(R.dimen.bottomNavigationBarHeight) + dpToPx(16f)
            marginEnd = dpToPx(16f)
            expandedFabContainer.layoutParams = this
        }

        FabTransform.setup(this, expandedFabContainer)

        findViewById<View>(android.R.id.content).setOnClickListener {
            onBackPressed()
        }

        newNoteButton.setOnClickListener {
            finish()
            startActivity(Intent(this, NoteActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        newListButton.setOnClickListener {
            finish()
            startActivity(Intent(this, NoteActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_IS_NOTE_LIST, true))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        newDrawingButton.setOnClickListener {
            finish()
            startActivity(Intent(this, NoteActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_ADD_DRAWING, true))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        addImageButton.setOnClickListener {
            finish()
            startActivity(Intent(this, NoteActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_ADD_IMAGE, true))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() = ActivityCompat.finishAfterTransition(this)
}