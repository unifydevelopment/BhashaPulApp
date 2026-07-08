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
    private var lastCallTime: Long = 0L
    private val MIN_INTERVAL_MS = 800L // small debounce so a double-tap doesn't fire twice

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Only react to the user tapping / long-pressing / selecting text on a message
        val relevantEvent = event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
        if (!relevantEvent) return

        if (event.packageName == packageName) return

        val tappedText = extractTextFromEvent(event)
        if (tappedText.isNullOrBlank()) return

        val now = System.currentTimeMillis()
        if (tappedText == lastText && now - lastCallTime < MIN_INTERVAL_MS) return
        lastCallTime = now
        lastText = tappedText

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val targetLang = prefs.getString(Prefs.TARGET_LANG, "hi") ?: "hi"

        overlayManager.showTranslation("Translate ho raha hai...")

        executor.execute {
            val translated = TranslationClient.translate(tappedText.take(400), targetLang)
            mainHandler.post {
                if (translated != null) {
                    overlayManager.showTranslation(translated)
                } else {
                    Toast.makeText(this, "Fail: " + TranslationClient.lastError, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Try to get the text of the exact node the user interacted with.
    // Falls back to walking a couple of levels up/down if the node itself has no text
    // (many messaging apps put the text on a child or parent view of the clicked row).
    private fun extractTextFromEvent(event: AccessibilityEvent): String? {
        val source = event.source
        if (source != null) {
            val direct = findTextInNode(source, 0)
            if (!direct.isNullOrBlank()) return direct
        }
        val fromEventText = event.text?.joinToString(" ")?.trim()
        if (!fromEventText.isNullOrBlank()) return fromEventText
        return null
    }

    private fun findTextInNode(node: AccessibilityNodeInfo, depth: Int): String? {
        if (depth > 4) return null
        node.text?.let { if (it.isNotBlank()) return it.toString() }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findTextInNode(child, depth + 1)
            if (!found.isNullOrBlank()) return found
        }
        return null
    }

    override fun onInterrupt() {
        overlayManager.hide()
    }
}
