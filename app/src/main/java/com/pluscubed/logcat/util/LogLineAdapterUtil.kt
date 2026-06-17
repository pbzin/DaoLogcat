package com.pluscubed.logcat.util
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pb.daologcat.R
object LogLineAdapterUtil {
    const val LOG_WTF = 100
    @JvmStatic fun getBackgroundColorForLogLevel(context: Context, logLevel: Int): Int {
        val res = when (logLevel) {
            Log.DEBUG -> R.color.background_debug
            Log.ERROR -> R.color.background_error
            Log.INFO -> R.color.background_info
            Log.VERBOSE -> R.color.background_verbose
            Log.WARN -> R.color.background_warn
            LOG_WTF -> R.color.background_wtf
            else -> android.R.color.black
        }
        return ContextCompat.getColor(context, res)
    }
    @JvmStatic fun getOrCreateTagColor(context: Context, tag: String?): Int {
        val hashCode = tag?.hashCode() ?: 0
        val smear = Math.abs(hashCode) % 17
        return PreferenceHelper.getColorScheme(context).getTagColors(context)[smear]
    }
    @JvmStatic fun logLevelIsAcceptableGivenLogLevelLimit(logLevel: Int, logLevelLimit: Int): Boolean {
        val minVal = when (logLevel) {
            Log.VERBOSE -> 0
            Log.DEBUG -> 1
            Log.INFO -> 2
            Log.WARN -> 3
            Log.ERROR -> 4
            LOG_WTF -> 5
            else -> return true
        }
        return minVal >= logLevelLimit
    }
}
