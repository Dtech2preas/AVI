package com.example.aviatorbot

import android.content.Context
import android.webkit.JavascriptInterface

class WebAppInterface(
    private val context: Context,
    private val onLog: (String) -> Unit,
    private val onHistory: (String) -> Unit
) {

    @JavascriptInterface
    fun log(message: String) {
        onLog(message)
    }

    @JavascriptInterface
    fun sendHistory(history: String) {
        onHistory(history)
    }

    @JavascriptInterface
    fun onMultiplier(value: String) {
        // value might be "1.23x"
        onLog("Multiplier: $value")
    }
}
