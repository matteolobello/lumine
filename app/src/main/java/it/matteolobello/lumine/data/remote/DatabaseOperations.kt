package it.matteolobello.lumine.data.remote

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.*
import java.io.File
import java.lang.ref.WeakReference

object DatabaseOperations {

    fun uploadNotes(context: Context, notes: ArrayList<Note>, progressHandler: (progress: Int) -> Unit = { _ -> }) {
        var opCounter = 0

        Handler(context.mainLooper).post {
            for (note in notes) {
                EncodeImagesAsyncTask(WeakReference(context), note.images) {
                    note.images = it

                    FirebaseDatabase.getInstance()
                            .reference
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(note.id)
                            .setValue(note)
                            .addOnCompleteListener { task ->
                                opCounter++
                                progressHandler(opCounter * 100 / notes.size)

                                log("OP COUNTER $opCounter")
                            }
                }.execute()
            }
        }
    }

    fun downloadNotesAsync(context: Context, callback: (notes: ArrayList<Note>) -> Unit = {}) {
        Handler(context.mainLooper).post {
            FirebaseDatabase.getInstance()
                    .reference
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .orderByKey()
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            callback(arrayListOf())
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val notes = arrayListOf<Note>()

                            var opCounter = 0
                            dataSnapshot.children.forEach {
                                val note: Note? = try {
                                    it.getValue(Note::class.java)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }

                                if (note != null) {
                                    DecodeImagesAsyncTask(WeakReference(context), note.images) {
                                        note.images = it

                                        notes.add(note)

                                        if (++opCounter == dataSnapshot.children.count()) {
                                            callback(notes)
                                        }
                                    }.execute()
                                } else {
                                    opCounter++
                                }
                            }
                        }
                    })
        }
    }

    fun removeAll(context: Context, callback: () -> Unit) {
        log("DatabaseOperations -> removeAll")

        Handler(context.mainLooper).post {
            FirebaseDatabase.getInstance()
                    .reference
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(null) { _, _ ->
                        callback()
                    }
        }
    }

    private class EncodeImagesAsyncTask(
            private val contextWeakRef: WeakReference<Context>,
            private val imagePaths: ArrayList<String>,
            private val callback: (encodedImages: ArrayList<String>) -> Unit
    ) : AsyncTask<Unit, Unit, ArrayList<String>>() {

        override fun doInBackground(vararg unit: Unit?): ArrayList<String> {
            val encodedImages = arrayListOf<String>()

            imagePaths.forEach {
                encodedImages.add(File(it).toBitmap().encode(contextWeakRef.get()!!))
            }

            return encodedImages
        }

        override fun onPostExecute(result: ArrayList<String>) {
            super.onPostExecute(result)

            callback(result)
        }
    }

    private class DecodeImagesAsyncTask(
            private val contextWeakRef: WeakReference<Context>,
            private val encodedImages: ArrayList<String>,
            private val callback: (encodedImages: ArrayList<String>) -> Unit
    ) : AsyncTask<Unit, Unit, ArrayList<String>>() {

        override fun doInBackground(vararg unit: Unit?): ArrayList<String> {
            val decodedImages = arrayListOf<String>()

            encodedImages.forEach {
                decodedImages.add(it.decodeBitmap().saveToCache(contextWeakRef.get()!!).path)
            }

            return decodedImages
        }

        override fun onPostExecute(result: ArrayList<String>) {
            super.onPostExecute(result)

            callback(result)
        }
    }
}