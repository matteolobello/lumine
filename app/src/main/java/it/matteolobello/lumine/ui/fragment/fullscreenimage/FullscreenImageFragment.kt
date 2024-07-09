package it.matteolobello.lumine.ui.fragment.fullscreenimage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.extension.toBitmap
import kotlinx.android.synthetic.main.fragment_fullscreen_image.*
import java.io.File
import kotlin.concurrent.thread

class FullscreenImageFragment : Fragment() {

    companion object {
        fun newInstance(imagePath: String) = FullscreenImageFragment().apply {
            arguments = Bundle().apply {
                putString(BundleKeys.EXTRA_IMAGE_PATH, imagePath)
            }
            retainInstance = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_fullscreen_image, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        thread {
            val bitmap = File(arguments!!.getString(BundleKeys.EXTRA_IMAGE_PATH)).toBitmap()
            photoView.post {
                photoView.setImageBitmap(bitmap)
            }
        }
    }
}