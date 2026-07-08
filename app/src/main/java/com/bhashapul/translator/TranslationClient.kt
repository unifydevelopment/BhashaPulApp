package com.bhashapul.translator

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslationClient {

    // Free translation API, no key needed. Rate-limited — fine for personal use,
    // swap for Google Cloud Translate / DeepL API if you need higher volume.
    fun translate(text: String, targetLang: String): String? {
        if (text.isBlank()) return null
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=autodetect|$targetLang")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            json.getJSONObject("responseData").getString("translatedText")
        } catch (e: Exception) {
            null
        }
    }
}
