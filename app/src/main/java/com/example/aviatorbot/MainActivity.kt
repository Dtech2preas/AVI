package com.example.aviatorbot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), WebAppInterface.Listener, StrategyEngine.Callback {

    private lateinit var webView: WebView
    private lateinit var tvLogs: TextView
    private lateinit var tvStatus: TextView
    private lateinit var svLogs: ScrollView
    private lateinit var btnLive: Button
    private lateinit var btnDemo: Button
    private lateinit var fabSettings: FloatingActionButton

    private lateinit var strategyEngine: StrategyEngine
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupWebView()
        setupListeners()

        strategyEngine = StrategyEngine(this)
    }

    private fun initViews() {
        webView = findViewById(R.id.webview)
        tvLogs = findViewById(R.id.tv_logs)
        tvStatus = findViewById(R.id.tv_status)
        svLogs = findViewById(R.id.log_scroll_view)
        btnLive = findViewById(R.id.btn_live)
        btnDemo = findViewById(R.id.btn_demo)
        fabSettings = findViewById(R.id.fab_settings)
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        // Set a realistic UserAgent to avoid detection/rendering issues
        webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectJavaScript()
            }
        }

        webView.loadUrl("https://www.betway.co.za/")
    }

    private fun setupListeners() {
        btnLive.setOnClickListener {
            strategyEngine.isLiveMode = true
            strategyEngine.isRunning = !strategyEngine.isRunning
            updateStatus()
        }

        btnDemo.setOnClickListener {
            strategyEngine.isLiveMode = false
            strategyEngine.isRunning = !strategyEngine.isRunning
            updateStatus()
        }

        fabSettings.setOnClickListener {
            // Toggle Logs visibility
            if (svLogs.visibility == View.VISIBLE) {
                svLogs.visibility = View.GONE
            } else {
                svLogs.visibility = View.VISIBLE
            }
        }
    }

    private fun updateStatus() {
        if (!strategyEngine.isRunning) {
            tvStatus.text = getString(R.string.status_waiting)
            tvStatus.setBackgroundColor(getColor(R.color.transparent_black))
        } else {
            if (strategyEngine.isLiveMode) {
                tvStatus.text = getString(R.string.status_active_live)
                tvStatus.setBackgroundColor(getColor(R.color.red))
            } else {
                tvStatus.text = getString(R.string.status_active_demo)
                tvStatus.setBackgroundColor(getColor(R.color.green))
            }
        }
    }

    fun injectJavaScript() {
        try {
            val inputStream = resources.openRawResource(R.raw.bot_script)
            val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            val js = sb.toString()
            webView.evaluateJavascript(js, null)
        } catch (e: Exception) {
            e.printStackTrace()
            appendLog("Error injecting JS: " + e.message)
        }
    }

    // --- WebAppInterface.Listener Implementation ---

    override fun onMultiplierUpdate(multiplier: Double) {
        runOnUiThread {
            strategyEngine.onMultiplierUpdate(multiplier)
        }
    }

    override fun onGameCrash(crashValue: Double) {
        runOnUiThread {
            strategyEngine.onGameCrash(crashValue)
        }
    }

    override fun onGameStart() {
        runOnUiThread {
            strategyEngine.onGameStart()
        }
    }

    override fun log(message: String) {
        runOnUiThread {
            appendLog(message)
        }
    }

    // --- StrategyEngine.Callback Implementation ---

    override fun performClick(action: String) {
        // Call JS to click button
        runOnUiThread {
             webView.evaluateJavascript("clickButton('$action');", null)
             appendLog("Action Triggered: $action")
        }
    }

    override fun onStatsUpdated(wins: Int, losses: Int, balance: Double) {
        runOnUiThread {
            appendLog("Stats: Wins=$wins, Losses=$losses, Bal=$balance")
        }
    }

    private fun appendLog(msg: String) {
        val currentText = tvLogs.text.toString()
        val newText = "$msg\n$currentText"
        tvLogs.text = newText
        // Limit log size maybe?
    }
}
