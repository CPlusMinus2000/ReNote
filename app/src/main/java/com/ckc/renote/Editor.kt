package com.ckc.renote

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
class Editor(
    private val webview: WebView,
    private val noteDao: NoteDao
) : WebViewClient() {

    private val undoManager: UndoManager = UndoManager()
    private var undoing = false
    private val rewinder: Rewinder = Rewinder()

    init {
        webview.webViewClient = this
        webview.loadUrl("file:///android_res/raw/editor.html")
        webview.settings.javaScriptEnabled = true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url
        if (url.scheme == "change") {
            if (!undoing) {
                undoManager.addState(State(url.toString().substring(9)))
                rewinder.addState(State(url.toString().substring(9)))
            }
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    fun setEditable(editable: Boolean) = webview.evaluateJavascript(
        "document.getElementById('editor').contentEditable = $editable", null)

    fun setContent(text: String) {
        webview.evaluateJavascript("document.getElementById('editor').innerHTML = \"$text\"") {
            undoing = false
        }
    }

    fun save(currNote: Note) = webview.evaluateJavascript("document.getElementById('editor').innerHTML") {
        currNote.contents = it
        currNote.lastEdited = System.currentTimeMillis()
        // file.writeText(Json.encodeToString(currNote), Charsets.UTF_8)
        noteDao!!.insert(currNote)
    }

    fun load(text: String) {
        val trimmed = text.trim('\"')
        setContent(trimmed)
        undoManager.initState(State(trimmed))
    }

    fun bold() = webview.evaluateJavascript("document.execCommand('bold')", null)

    fun italic() = webview.evaluateJavascript("document.execCommand('italic')", null)

    fun underline() = webview.evaluateJavascript("document.execCommand('underline')", null)

    fun strikeThrough() = webview.evaluateJavascript("document.execCommand('strikeThrough')", null)

    fun increaseFontSize() = webview.evaluateJavascript("changeFontSize('big')", null)

    fun decreaseFontSize() = webview.evaluateJavascript("changeFontSize('small')", null)

    fun undo() {
        undoing = true
        setContent(undoManager.undo().content)
    }

    fun redo() {
        undoing = true
        setContent(undoManager.redo().content)
    }

    fun startRecording() = rewinder.startRecording(undoManager.getCurrentState())

    fun stopRecording() = rewinder.stopRecording()

    fun play() {
        rewinder.playRecording(this, undoManager.getCurrentState())
    }
}
