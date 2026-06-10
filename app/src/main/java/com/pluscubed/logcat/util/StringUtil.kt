package com.pluscubed.logcat.util

object StringUtil {
    @JvmStatic
    fun nullToEmpty(str: CharSequence?): String = str?.toString() ?: ""

    @JvmStatic
    fun containsIgnoreCase(str: String?, query: String?): Boolean {
        if (str == null || query == null) return false
        return str.lowercase().contains(query.lowercase())
    }
}
