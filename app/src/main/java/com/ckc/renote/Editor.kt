package com.ckc.renote

import android.annotation.SuppressLint
import android.webkit.WebView
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
class Editor(
    private val webview: WebView
) {
    init {
        webview.loadUrl("file:///android_res/raw/editor.html")
        webview.settings.javaScriptEnabled = true
    }

    fun save(file: File) = webview.evaluateJavascript("document.getElementById('editor').innerHTML") { s ->
        file.writeText(s, Charsets.UTF_8)
    }

    fun load(text: String) = webview.evaluateJavascript("document.getElementById('editor').innerHTML = $text", null)

    fun bold() = webview.evaluateJavascript("document.execCommand('bold')", null)

    fun italic() = webview.evaluateJavascript("document.execCommand('italic')", null)

    fun underline() = webview.evaluateJavascript("document.execCommand('underline')", null)

    fun strikeThrough() = webview.evaluateJavascript("document.execCommand('strikeThrough')", null)

    fun increaseFontSize() = webview.evaluateJavascript("document.execCommand('increaseFontSize')", null)

    fun decreaseFontSize() = webview.evaluateJavascript("document.execCommand('decreaseFontSize')", null)
}
