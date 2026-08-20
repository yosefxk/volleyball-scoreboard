package com.volleyball.scoreboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var webView: WebView
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake at all times during match
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen immersive mode
        hideSystemUI()

        // Init TextToSpeech
        tts = TextToSpeech(this, this)

        // Setup WebView
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidNative")
            loadUrl("file:///android_asset/index.html")
        }

        setContentView(webView)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(1.05f)
            isTtsReady = true
        }
    }

    /**
     * NATIVE HARDWARE KEY INTERCEPTION:
     * Intercepts physical Volume and Shutter clicker buttons from AB Shutter 3.
     * Returning TRUE consumes the event so Android NEVER changes the audio volume or shows the volume slider!
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    triggerTeamScore("team1", "VolumeUp")
                    return true // Consumed - blocks Android volume popup!
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    triggerTeamScore("team2", "VolumeDown")
                    return true // Consumed - blocks Android volume popup!
                }
                KeyEvent.KEYCODE_CAMERA, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                    triggerTeamScore("team1", "ShutterButton1")
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    triggerTeamScore("team2", "ShutterButton2")
                    return true
                }
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_CAMERA, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun triggerTeamScore(team: String, source: String) {
        runOnUiThread {
            webView.evaluateJavascript(
                "window.handleNativeRemoteClick && window.handleNativeRemoteClick('$team', '$source');",
                null
            )
        }
    }

    fun speakText(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "score_announcement")
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
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    inner class WebAppInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun speak(text: String) {
            activity.speakText(text)
        }

        @JavascriptInterface
        fun vibrate(duration: Long) {
            activity.vibratePhone(duration)
        }
    }
}
