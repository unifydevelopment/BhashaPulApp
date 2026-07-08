package com.bhashapul.translator

// Lightweight cleanup for dictated text: trims filler words and tidies
// spacing/capitalization. This is rule-based, not full AI grammar correction —
// good enough for quick dictation, but not a replacement for a real LLM pass.
object TextCleaner {

    private val fillerWords = listOf(
        "\\bumm+\\b", "\\buh+\\b", "\\byou know\\b", "\\blike,\\b", "\\bmatlab\\b,"
    )

    fun clean(raw: String): String {
        var text = raw.trim()
        for (pattern in fillerWords) {
            text = text.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }
        text = text.replace(Regex("\\s+"), " ").trim()
        if (text.isNotEmpty()) {
            text = text[0].uppercaseChar() + text.substring(1)
        }
        return "$text "
    }
}
