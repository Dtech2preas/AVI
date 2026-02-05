package com.example.aviatorbot

import android.webkit.JavascriptInterface

class WebAppInterface(private val listener: Listener) {

    interface Listener {
        fun onMultiplierUpdate(multiplier: Double)
        fun onGameCrash(crashValue: Double)
        fun onGameStart()
        fun log(message: String)
    }

    @JavascriptInterface
    fun updateMultiplier(multiplier: String) {
        try {
            val value = multiplier.replace("x", "").trim().toDouble()
            listener.onMultiplierUpdate(value)
        } catch (e: Exception) {
            listener.log("Error parsing multiplier: $multiplier")
        }
    }

    @JavascriptInterface
    fun gameCrash(value: String) {
        try {
            val crashValue = value.replace("x", "").trim().toDouble()
            listener.onGameCrash(crashValue)
        } catch (e: Exception) {
            listener.log("Error parsing crash value: $value")
        }
    }

    @JavascriptInterface
    fun gameStart() {
        listener.onGameStart()
    }

    @JavascriptInterface
    fun logFromJs(msg: String) {
        listener.log("JS: $msg")
    }
}
