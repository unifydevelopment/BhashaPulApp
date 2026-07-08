package com.bhashapul.translator

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.Executors

class TranslateAccessibilityService : AccessibilityService() {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var overlayManager: OverlayManager
    private var lastText: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val root = rootInActiveWindow ?: return

        val screenText = collectVisibleText(root).trim()
        if (screenText.isEmpty() || screenText == lastText) return
        lastText = screenText

        // Skip our own app to avoid translating the settings screen
        if (event.packageName == packageName) return

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val targetLang = prefs.getString(Prefs.TARGET_LANG, "hi") ?: "hi"

        executor.execute {
            val translated = TranslationClient.translate(screenText.take(400), targetLang)
            if (translated != null) {
                overlayManager.showTranslation(translated)
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
