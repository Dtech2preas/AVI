package com.example.aviatorbot

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnStart: Button
    private lateinit var btnMode: Button
    private lateinit var tvStatus: TextView

    private var isBotRunning = false
    private var isLiveMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        btnStart = findViewById(R.id.btn_start)
        btnMode = findViewById(R.id.btn_mode)
        tvStatus = findViewById(R.id.tv_status)

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
                    tvStatus.text = "Log: $msg"
                }
            },
            onHistory = { history ->
                // history might be comma separated values from JS
                val values = history.split(",").mapNotNull {
                    it.replace("x", "").trim().toDoubleOrNull()
                }
                values.forEach { StrategyEngine.onRoundCrash(it) }

                runOnUiThread {
                    tvStatus.text = StrategyEngine.getStats()

                    if (isBotRunning && isLiveMode) {
                        if (StrategyEngine.shouldBet()) {
                            // trigger bet
                            webView.evaluateJavascript("window.botClickBet();", null)
                        }
                    }
                }
            }
        ), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                tvStatus.text = "Loaded: $url"
                injectBotScript()
            }
        }

        // Load Betway
        webView.loadUrl("https://www.betway.co.za/lobby/casino")
    }

    private fun injectBotScript() {
        val js = """
            (function() {
                if (window.botInjected) return;
                window.botInjected = true;
                window.Android.log("Bot Injected via JS");

                function scan() {
                    // Scan for 'Bet' buttons
                    // We look for green buttons with text 'Bet'
                    // This is a heuristic based on the screenshot

                    let buttons = Array.from(document.querySelectorAll('button, div[role="button"], .btn'));
                    let betBtn = buttons.find(b => {
                        let text = b.innerText || "";
                        return text.includes("Bet") && b.offsetHeight > 0; // Visible
                    });

                    if (betBtn) {
                         // found
                    }
                }

                setInterval(scan, 2000);
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun setupControls() {
        btnStart.setOnClickListener {
            isBotRunning = !isBotRunning
            btnStart.text = if (isBotRunning) "Stop Bot" else "Start Bot"
            updateStatus()
        }

        btnMode.setOnClickListener {
            isLiveMode = !isLiveMode
            btnMode.text = if (isLiveMode) "Mode: LIVE" else "Mode: Demo"
            updateStatus()
        }
    }

    private fun updateStatus() {
        val state = if (isBotRunning) "Running" else "Idle"
        val mode = if (isLiveMode) "LIVE" else "Demo"
        tvStatus.text = "Status: $state ($mode)"
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
