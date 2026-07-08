package com.bhashapul.translator

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ScrollView? = null
    private var textView: TextView? = null

    fun showTranslation(text: String) {
        if (overlayView == null) {
            createOverlay()
        }
        textView?.text = text
        overlayView?.visibility = android.view.View.VISIBLE
    }

    private fun createOverlay() {
        val tv = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(28, 20, 28, 20)
        }
        textView = tv

        val scroll = ScrollView(context).apply {
            setBackgroundColor(Color.parseColor("#DD171A2B"))
            addView(tv)
        }
        overlayView = scroll

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 60
        }

        try {
            windowManager.addView(scroll, params)
        } catch (e: Exception) {
            // Overlay permission likely not granted yet
        }
    }

    fun hide() {
        overlayView?.visibility = android.view.View.GONE
    }
}
