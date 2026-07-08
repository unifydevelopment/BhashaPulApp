package com.bhashapul.translator

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

class TranslateAccessibilityService : AccessibilityService() {

    companion object {
        // Other components (like the floating mic) use this to reach the
        // running accessibility service instance.
        var instance: TranslateAccessibilityService? = null
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var overlayManager: OverlayManager
    private var lastText: String = ""
    private var lastCallTime: Long = 0L
    private val MIN_INTERVAL_MS = 800L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        overlayManager = OverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

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

    // Finds the currently focused editable text field (wherever the user's
    // cursor is - a WhatsApp message box, a browser field, anything) and
    // inserts the given text at the cursor position via clipboard + paste.
    fun insertTextIntoFocusedField(text: String): Boolean {
        val focused = findFocusedEditableNode() ?: return false
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("bhasha_pul_dictation", text))
        return focused.performAction(AccessibilityNodeInfo.ACTION_PASTE)
    }

    private fun findFocusedEditableNode(): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) return focused
        return null
    }

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

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
