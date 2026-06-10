package com.pluscubed.logcat.util

import android.util.Log

class UtilLogger {
    private val tag: String
    constructor(tag: String) { this.tag = tag }
    constructor(clazz: Class<*>) { this.tag = clazz.simpleName }

    private fun log(level: Int, msg: String, e: Throwable? = null) {
        try {
            if (e != null) Log.e(tag, msg, e) else Log.println(level, tag, msg)
        } catch (ignored: Exception) {
            println("$tag: $msg")
        }
    }

    fun i(format: String, vararg more: Any?) { log(4, String.format(format, *more)) }
    fun i(e: Exception, format: String, vararg more: Any?) { log(4, String.format(format, *more), e) }
    fun w(e: Exception, format: String, vararg more: Any?) { log(5, String.format(format, *more), e) }
    fun w(format: String, vararg more: Any?) { log(5, String.format(format, *more)) }
    fun e(format: String, vararg more: Any?) { log(6, String.format(format, *more)) }
    fun e(e: Exception, format: String, vararg more: Any?) { log(6, String.format(format, *more), e) }
    fun d(format: String, vararg more: Any?) { if (DEBUG_MODE) log(3, String.format(format, *more)) }
    fun d(e: Exception, format: String, vararg more: Any?) { if (DEBUG_MODE) log(3, String.format(format, *more), e) }

    companion object { const val DEBUG_MODE = false }
}
