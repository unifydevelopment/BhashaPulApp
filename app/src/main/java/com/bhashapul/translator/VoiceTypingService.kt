package com.bhashapul.translator

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat

class VoiceTypingService : Service() {

    private lateinit var windowManager: WindowManager
    private var micView: TextView? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var listening = false

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addMicButton()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val channelId = "voice_typing_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Voice Typing", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bhasha Pul: voice typing on hai")
            .setContentText("Kisi bhi text box mein tap karke mic dabayein")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(1, notification)
    }

    private fun addMicButton() {
        val tv = TextView(this).apply {
            text = "\uD83C\uDFA4"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#1F4A48"))
                setStroke(3, Color.parseColor("#3E8F8B"))
            }
        }
        micView = tv

        val size = (56 * resources.displayMetrics.density).toInt()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            size, size, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        tv.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 12 || Math.abs(dy) > 12) isDragging = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) toggleListening()
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(tv, params)
        } catch (e: Exception) {
            // overlay permission missing
        }
    }

    private fun toggleListening() {
        if (listening) {
            speechRecognizer?.stopListening()
            return
        }
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val spokenLang = prefs.getString(Prefs.VOICE_INPUT_LANG, "hi-IN") ?: "hi-IN"

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, spokenLang)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listening = true
                micView?.setBackgroundColor(Color.RED)
                setMicListeningStyle(true)
            }
            override fun onResults(results: Bundle?) {
                listening = false
                setMicListeningStyle(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val said = matches?.firstOrNull()
                if (!said.isNullOrBlank()) {
                    val cleaned = TextCleaner.clean(said)
                    val inserted = TranslateAccessibilityService.instance?.insertTextIntoFocusedField(cleaned) ?: false
                    if (!inserted) {
                        android.widget.Toast.makeText(
                            this@VoiceTypingService,
                            "Koi text box focus mein nahi mila. Pehle us box mein tap karein.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
                speechRecognizer?.destroy()
            }
            override fun onError(error: Int) {
                listening = false
                setMicListeningStyle(false)
                speechRecognizer?.destroy()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun setMicListeningStyle(isListening: Boolean) {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (isListening) Color.parseColor("#D9714E") else Color.parseColor("#1F4A48"))
            setStroke(3, Color.parseColor("#3E8F8B"))
        }
        micView?.background = drawable
    }

    override fun onDestroy() {
        super.onDestroy()
        micView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        speechRecognizer?.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
