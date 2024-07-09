package it.matteolobello.lumine.data.ocr

import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import it.matteolobello.lumine.extension.toBitmap
import java.io.File
import java.lang.ref.WeakReference

class OcrAsyncTask(private val contextWeakRef: WeakReference<Context>,
                   private val file: File,
                   private val callback: (output: String) -> Unit) : AsyncTask<Any?, Any?, String>() {

    override fun doInBackground(vararg p0: Any?): String {
        val bitmap = file.toBitmap()

        val textRecognizer = TextRecognizer.Builder(contextWeakRef.get()).build()

        val imageFrame = Frame.Builder()
                .setBitmap(bitmap)
                .build()

        var imageText = ""

        val textBlocks = textRecognizer.detect(imageFrame)
        for (i in 0..textBlocks.size()) {
            textBlocks[i]?.let {
                imageText += "${it.value}\n\n"
            }
        }

        return imageText
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        callback(result)
    }
}