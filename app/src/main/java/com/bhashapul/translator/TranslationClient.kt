package com.bhashapul.translator

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslationClient {

    var lastError: String? = null
 private const val CONTACT_EMAIL = "backenddeveloper111@gmail.com"
    fun translate(text: String, targetLang: String): String? {
        if (text.isBlank()) return null
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=autodetect|$targetLang")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")

            val code = conn.responseCode
            if (code != 200) {
                lastError = "HTTP code: $code"
                return null
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val result = json.getJSONObject("responseData").getString("translatedText")
            lastError = null
            result
        } catch (e: Exception) {
            lastError = e.javaClass.simpleName + ": " + e.message
            null
        }
    }
}
