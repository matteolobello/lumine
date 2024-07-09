package it.matteolobello.lumine.extension

import it.matteolobello.lumine.data.model.Note

fun ArrayList<Note>.debug() {
    log("==========================================")
    log()
    forEach {
        log("---------------------------------")
        log("Note ID    \t ${it.id}")
        log("Note Title \t ${it.title}")
        log("---------------------------------")
    }
    log()
    log("==========================================")
}