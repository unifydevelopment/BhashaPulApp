package com.bhashapul.translator

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val languages = listOf(
        "Hindi" to "hi",
        "English" to "en",
        "Urdu" to "ur",
        "Arabic" to "ar",
        "Spanish" to "es",
        "French" to "fr",
        "Bengali" to "bn",
        "Punjabi" to "pa"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val spinner = findViewById<Spinner>(R.id.targetLangSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages.map { it.first })
        spinner.adapter = adapter

        val savedCode = prefs.getString(Prefs.TARGET_LANG, "hi")
        val savedIndex = languages.indexOfFirst { it.second == savedCode }
        if (savedIndex >= 0) spinner.setSelection(savedIndex)

        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString(Prefs.TARGET_LANG, languages[position].second).apply()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        findViewById<Button>(R.id.btnOverlayPermission).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                findViewById<TextView>(R.id.statusText).text = "Overlay permission pehle se hi di hui hai."
            }
        }

        findViewById<Button>(R.id.btnAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}

object Prefs {
    const val NAME = "bhasha_pul_prefs"
    const val TARGET_LANG = "target_lang"
}
