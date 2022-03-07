package com.ckc.renote

import android.annotation.SuppressLint
import android.webkit.WebView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
class Editor(
    private val webview: WebView
) {
    init {
        webview.loadUrl("file:///android_res/raw/editor.html")
        webview.settings.javaScriptEnabled = true
    }

    fun save(file: File, currNote: Note) = webview.evaluateJavascript("document.getElementById('editor').innerHTML") { s ->
        currNote.contents = s
        currNote.lastEdited = System.currentTimeMillis()
        file.writeText(Json.encodeToString(currNote), Charsets.UTF_8)
    }

    fun load(text: String) = webview.evaluateJavascript("document.getElementById('editor').innerHTML = $text", null)

    fun bold() = webview.evaluateJavascript("document.execCommand('bold')", null)

    fun italic() = webview.evaluateJavascript("document.execCommand('italic')", null)

    fun underline() = webview.evaluateJavascript("document.execCommand('underline')", null)

    fun strikeThrough() = webview.evaluateJavascript("document.execCommand('strikeThrough')", null)

    private fun changeFontSize(change: String) {
        val script = """{
            let selected = window.getSelection().getRangeAt(0);
            let selectedText = selected.extractContents();
            let change = document.createElement('$change');
            change.appendChild(selectedText);
            selected.insertNode(change);
        }""".trimIndent()
        webview.evaluateJavascript(script, null)
    }

    fun increaseFontSize() = changeFontSize("big")

    fun decreaseFontSize() = changeFontSize("small")
}
