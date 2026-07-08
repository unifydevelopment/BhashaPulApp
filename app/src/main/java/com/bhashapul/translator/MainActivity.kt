package com.bhashapul.translator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val translateLanguages = listOf(
        "Hindi" to "hi",
        "English" to "en",
        "Urdu" to "ur",
        "Arabic" to "ar",
        "Spanish" to "es",
        "French" to "fr",
        "Bengali" to "bn",
        "Punjabi" to "pa"
    )

    // Speech recognizer needs locale-style codes, not plain 2-letter codes.
    private val voiceLanguages = listOf(
        "Hindi" to "hi-IN",
        "English" to "en-US",
        "Urdu" to "ur-PK",
        "Arabic" to "ar-SA",
        "Spanish" to "es-ES",
        "French" to "fr-FR",
        "Bengali" to "bn-BD",
        "Punjabi" to "pa-IN"
    )

    private val permissionRequestCode = 101
    private var voiceTypingOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val targetSpinner = findViewById<Spinner>(R.id.targetLangSpinner)
        targetSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, translateLanguages.map { it.first }
        )
        val savedTargetCode = prefs.getString(Prefs.TARGET_LANG, "hi")
        translateLanguages.indexOfFirst { it.second == savedTargetCode }.let {
            if (it >= 0) targetSpinner.setSelection(it)
        }
        targetSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString(Prefs.TARGET_LANG, translateLanguages[pos].second).apply()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        })

        val voiceSpinner = findViewById<Spinner>(R.id.voiceLangSpinner)
        voiceSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, voiceLanguages.map { it.first }
        )
        val savedVoiceCode = prefs.getString(Prefs.VOICE_INPUT_LANG, "hi-IN")
        voiceLanguages.indexOfFirst { it.second == savedVoiceCode }.let {
            if (it >= 0) voiceSpinner.setSelection(it)
        }
        voiceSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString(Prefs.VOICE_INPUT_LANG, voiceLanguages[pos].second).apply()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        })

        findViewById<Button>(R.id.btnOverlayPermission).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                findViewById<TextView>(R.id.statusText).text = "Overlay permission pehle se hi di hui hai."
            }
        }

        findViewById<Button>(R.id.btnAccessibilitySettings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnVoiceTyping).setOnClickListener {
            toggleVoiceTyping()
        }
    }

    private fun toggleVoiceTyping() {
        if (!Settings.canDrawOverlays(this)) {
            findViewById<TextView>(R.id.statusText).text = "Pehle overlay permission dein (step 1)."
            return
        }
        val micPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (micPermission != PackageManager.PERMISSION_GRANTED) {
            val toRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                toRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            ActivityCompat.requestPermissions(this, toRequest.toTypedArray(), permissionRequestCode)
            return
        }

        voiceTypingOn = !voiceTypingOn
        val btn = findViewById<Button>(R.id.btnVoiceTyping)
        if (voiceTypingOn) {
            startService(Intent(this, VoiceTypingService::class.java))
            btn.text = "3. Voice typing band karein"
            findViewById<TextView>(R.id.statusText).text =
                "Floating mic button dikh raha hai. Kisi bhi text box mein tap karke, mic dabakar boliye."
        } else {
            stopService(Intent(this, VoiceTypingService::class.java))
            btn.text = "3. Voice typing shuru karein"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toggleVoiceTyping()
        }
    }
}

object Prefs {
    const val NAME = "bhasha_pul_prefs"
    const val TARGET_LANG = "target_lang"
    const val VOICE_INPUT_LANG = "voice_input_lang"
}
