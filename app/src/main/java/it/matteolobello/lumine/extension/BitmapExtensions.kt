package it.matteolobello.lumine.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun File.toBitmap() = BitmapFactory.decodeFile(path)!!

fun Bitmap.encode(context: Context, maxWidth: Int = 600): String {
    val resizedBitmap = resizeBitmap(context, maxWidth)

    val stream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
    val byteFormat = stream.toByteArray()
    return Base64.encodeToString(byteFormat, Base64.NO_WRAP)
}

fun String.decodeBitmap(): Bitmap {
    val decodedString = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
}

fun Bitmap.saveToCache(context: Context): File {
    val folder = File("${context.cacheDir}/images")
    if (!folder.exists()) {
        folder.mkdir()
    }

    val file = File(folder, System.currentTimeMillis().toString())
    file.createNewFile()

    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray()

    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(bytes)
    fileOutputStream.flush()
    fileOutputStream.close()

    return file
}

private fun Bitmap.resizeBitmap(context: Context, width: Int = 600): Bitmap {
    val finalWidth = Math.min(width, this.width)
    val finalHeight = this.height * finalWidth / this.width

    val scaledBitmap =
            if (finalWidth == this.width
                    && finalHeight == this.height)
                this
            else
                Bitmap.createScaledBitmap(
                        this, finalWidth, finalHeight, false)

    val tmpFile = scaledBitmap.saveToCache(context)
    val outputBitmap = Compressor(context)
            .compressToBitmapAsFlowable(tmpFile)
            .blockingFirst()

    tmpFile.delete()

    return outputBitmap
}