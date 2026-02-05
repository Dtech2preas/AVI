package com.example.aviatorbot

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnStart: Button
    private lateinit var btnMode: Button
    private lateinit var btnSettings: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView

    private var isBotRunning = false
    private var isLiveMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        btnStart = findViewById(R.id.btn_start)
        btnMode = findViewById(R.id.btn_mode)
        btnSettings = findViewById(R.id.btn_settings)
        tvStatus = findViewById(R.id.tv_status)
        tvLogs = findViewById(R.id.tv_logs)

        setupWebView()
        setupControls()
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        // Spoof User Agent to ensure mobile site
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        webView.addJavascriptInterface(WebAppInterface(this,
            onLog = { msg ->
                runOnUiThread {
                    appendLog(msg)
                }
            },
            onHistory = { history ->
                // If JS sends history (comma separated)
                 try {
                     val values = history.split(",").mapNotNull {
                        it.replace("x", "").trim().toDoubleOrNull()
                    }
                    if (values.isNotEmpty()) {
                        values.forEach { StrategyEngine.onRoundCrash(it) }
                        runOnUiThread {
                            appendLog("History Sync: Added ${values.size} rounds.")
                            // Refresh target
                            updateBotState()
                        }
                    }
                 } catch (e: Exception) {
                     runOnUiThread { appendLog("Error parsing history: ${e.message}") }
                 }
            },
            onMultiplier = { valueStr ->
                runOnUiThread {
                     tvStatus.text = "Status: ${if (isBotRunning) "Running" else "Idle"} - $valueStr"
                }
            }
        ), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                appendLog("Loaded: $url")
                injectBotScript()
            }
        }

        // Load Betway
        webView.loadUrl("https://www.betway.co.za/lobby/casino")
    }

    private fun injectBotScript() {
        try {
            val inputStream = resources.openRawResource(R.raw.bot_script)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val js = reader.use { it.readText() }

            webView.evaluateJavascript(js, null)
            appendLog("Bot script injected.")

            // Sync initial state
            updateBotState()
        } catch (e: Exception) {
            appendLog("Error reading bot script: ${e.message}")
        }
    }

    private fun updateBotState() {
        val target = StrategyEngine.getNextTarget()
        val jsCommand = "if(window.updateBotParams) window.updateBotParams($isBotRunning, $target);"
        webView.evaluateJavascript(jsCommand, null)
        appendLog("Updated Bot: Running=$isBotRunning, Target=$target")

        // Also update stats log
        appendLog(StrategyEngine.getStats())
    }

    private fun setupControls() {
        btnStart.setOnClickListener {
            isBotRunning = !isBotRunning
            btnStart.text = if (isBotRunning) "Stop" else "Start"
            updateBotState()
        }

        btnMode.setOnClickListener {
            isLiveMode = !isLiveMode
            btnMode.text = if (isLiveMode) "Mode: LIVE" else "Mode: Demo"
            appendLog("Mode switched to ${if (isLiveMode) "LIVE" else "Demo"}")
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun appendLog(msg: String) {
        val current = tvLogs.text.toString()
        // Keep log size manageable
        if (current.length > 5000) {
            tvLogs.text = msg + "\n" + current.take(4000) + "..."
        } else {
            tvLogs.text = msg + "\n" + current
        }
    }

    override fun onResume() {
        super.onResume()
        // Update bot with potentially new settings
        updateBotState()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
