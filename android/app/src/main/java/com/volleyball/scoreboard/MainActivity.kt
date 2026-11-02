package com.volleyball.scoreboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var lastKeyDownTimeTeam1 = 0L
    private var lastKeyDownTimeTeam2 = 0L
    private var isKeyDownTeam1 = false
    private var isKeyDownTeam2 = false
    private val HARDWARE_DEBOUNCE_MS = 120L

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lock screen to Landscape orientation for pure horizontal scoreboard
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Keep screen awake at all times during match
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        webView.apply {
            setBackgroundColor(0xFF000000.toInt())
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                loadWithOverviewMode = true
                useWideViewPort = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }
            webChromeClient = WebChromeClient()
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidNative")
            loadUrl("file:///android_asset/index.html")
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * NATIVE HARDWARE KEY INTERCEPTOR:
     * - Forwards ACTION_DOWN (repeatCount == 0) and ACTION_UP to the JS scoring engine
     *   so short press = +1, long hold = -1, and rapid double-taps are ignored.
     * - Consumes event (returns true) so Android NEVER shows the volume slider!
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode
        val isTeam1Key = code == KeyEvent.KEYCODE_VOLUME_UP ||
            code == KeyEvent.KEYCODE_CAMERA ||
            code == KeyEvent.KEYCODE_ENTER ||
            code == KeyEvent.KEYCODE_DPAD_CENTER ||
            code == KeyEvent.KEYCODE_PAGE_UP ||
            code == KeyEvent.KEYCODE_BUTTON_A ||
            code == KeyEvent.KEYCODE_MEDIA_PREVIOUS

        val isTeam2Key = code == KeyEvent.KEYCODE_VOLUME_DOWN ||
            code == KeyEvent.KEYCODE_SPACE ||
            code == KeyEvent.KEYCODE_PAGE_DOWN ||
            code == KeyEvent.KEYCODE_BUTTON_B ||
            code == KeyEvent.KEYCODE_MEDIA_NEXT ||
            code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
            code == KeyEvent.KEYCODE_HEADSETHOOK

        if (isTeam1Key) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.repeatCount == 0 && !isKeyDownTeam1) {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastKeyDownTimeTeam1 >= HARDWARE_DEBOUNCE_MS) {
                        lastKeyDownTimeTeam1 = now
                        isKeyDownTeam1 = true
                        triggerRemoteKeyEvent("team1", "down")
                    }
                }
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                if (isKeyDownTeam1) {
                    isKeyDownTeam1 = false
                    triggerRemoteKeyEvent("team1", "up")
                }
                return true
            }
        } else if (isTeam2Key) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.repeatCount == 0 && !isKeyDownTeam2) {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastKeyDownTimeTeam2 >= HARDWARE_DEBOUNCE_MS) {
                        lastKeyDownTimeTeam2 = now
                        isKeyDownTeam2 = true
                        triggerRemoteKeyEvent("team2", "down")
                    }
                }
                return true
            } else if (event.action == KeyEvent.ACTION_UP) {
                if (isKeyDownTeam2) {
                    isKeyDownTeam2 = false
                    triggerRemoteKeyEvent("team2", "up")
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun triggerRemoteKeyEvent(team: String, action: String) {
        runOnUiThread {
            if (action == "down") {
                webView.evaluateJavascript(
                    "if (window.handleRemoteKeyDown) { window.handleRemoteKeyDown('$team'); } else if (window.handleRemoteClick) { window.handleRemoteClick('$team'); }",
                    null
                )
            } else {
                webView.evaluateJavascript(
                    "if (window.handleRemoteKeyUp) { window.handleRemoteKeyUp('$team'); }",
                    null
                )
            }
        }
    }

    fun vibratePhone(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideSystemUI() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun vibrate(duration: Long) {
            activity.vibratePhone(duration)
        }
    }
}
