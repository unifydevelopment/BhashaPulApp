package com.bhashapul.translator

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslationClient {

    var lastError: String? = null

    fun translate(text: String, targetLang: String): String? {
        if (text.isBlank()) return null
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = URL(
                "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encoded"
            )
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
            val outer = JSONArray(response)
            val segments = outer.getJSONArray(0)

            val builder = StringBuilder()
            for (i in 0 until segments.length()) {
                val segment = segments.getJSONArray(i)
                val piece = segment.optString(0, "")
                builder.append(piece)
            }
            lastError = null
            builder.toString()
        } catch (e: Exception) {
            lastError = e.javaClass.simpleName + ": " + e.message
            null
        }
    }
}
