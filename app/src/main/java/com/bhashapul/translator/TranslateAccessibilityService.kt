package com.bhashapul.translator

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class TranslateAccessibilityService : AccessibilityService() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var overlayManager: OverlayManager
    private var lastText: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
        mainHandler.post {
            Toast.makeText(this, "Bhasha Pul: service shuru ho gayi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val root = rootInActiveWindow ?: return

        val screenText = collectVisibleText(root).trim()
        if (screenText.isEmpty() || screenText == lastText) return
        lastText = screenText

        if (event.packageName == packageName) return

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val targetLang = prefs.getString(Prefs.TARGET_LANG, "hi") ?: "hi"

        mainHandler.post {
            Toast.makeText(this, "Text mila: " + screenText.take(30), Toast.LENGTH_SHORT).show()
        }

        executor.execute {
            val translated = TranslationClient.translate(screenText.take(400), targetLang)
            mainHandler.post {
                if (translated != null) {
                    Toast.makeText(this, "Translate ho gaya", Toast.LENGTH_SHORT).show()
                    overlayManager.showTranslation(translated)
                } else {
                    Toast.makeText(this, "Translation fail ho gaya (internet/API check karein)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun collectVisibleText(node: AccessibilityNodeInfo, depth: Int = 0): String {
        if (depth > 12) return ""
        val builder = StringBuilder()
        node.text?.let { if (it.isNotBlank()) builder.append(it).append(". ") }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            builder.append(collectVisibleText(child, depth + 1))
        }
        return builder.toString()
    }

    override fun onInterrupt() {
        overlayManager.hide()
    }
}
