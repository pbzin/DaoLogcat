package com.pluscubed.logcat.helper

import android.content.Context
import androidx.preference.PreferenceManager
import com.pluscubed.logcat.data.ColorScheme
import com.pluscubed.logcat.util.UtilLogger
import org.omnirom.logcat.R

object PreferenceHelper {
    const val THEME_SYSTEM = 0
    const val THEME_DARK = 1
    const val THEME_LIGHT = 2

    private const val WIDGET_EXISTS_PREFIX = "widget_"
    private const val FULL_BUFFER_MIGRATION_KEY = "daologcat_full_buffer_migrated"
    private const val THEME_MODE_KEY = "daologcat_theme_mode"
    private var textSize = -1f
    private var defaultLogLevel: Char? = null
    private var showTimestampAndPid: Boolean? = null
    private var displayLimit = -1
    private val log = UtilLogger(PreferenceHelper::class.java)

    @JvmStatic fun clearCache() {
        defaultLogLevel = null
        textSize = -1f
        showTimestampAndPid = null
        displayLimit = -1
    }

    @JvmStatic fun getBuffers(context: Context): Set<String> {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultValue = setOf(context.getString(R.string.pref_buffer_choice_all_value))
        val key = context.getString(R.string.pref_buffer)
        val allBuffer = context.getString(R.string.pref_buffer_choice_all_value)
        if (!sharedPrefs.getBoolean(FULL_BUFFER_MIGRATION_KEY, false)) {
            sharedPrefs.edit().putStringSet(key, defaultValue).putBoolean(FULL_BUFFER_MIGRATION_KEY, true).apply()
            return defaultValue
        }
        val buffers = sharedPrefs.getStringSet(key, defaultValue) ?: defaultValue
        return if (buffers.contains(allBuffer)) setOf(allBuffer) else buffers
    }

    @JvmStatic fun getDisplayLimitPreference(context: Context): Int {
        if (displayLimit == -1) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val defaultValue = context.getString(R.string.pref_display_limit_default)
            val key = context.getString(R.string.pref_display_limit)
            val value = sharedPrefs.getString(key, defaultValue) ?: defaultValue
            displayLimit = try { value.toInt() } catch (e: Exception) { defaultValue.toInt() }
        }
        return displayLimit
    }

    @JvmStatic fun setDisplayLimitPreference(context: Context, limit: Int) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.pref_display_limit)
        sharedPrefs.edit().putString(key, limit.toString()).apply()
        displayLimit = limit
    }

    @JvmStatic fun getShowTimestampAndPidPreference(context: Context): Boolean {
        if (showTimestampAndPid == null) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            showTimestampAndPid = sharedPrefs.getBoolean(context.getString(R.string.pref_show_timestamp), true)
        }
        return showTimestampAndPid!!
    }

    @JvmStatic fun getTextSizePreference(context: Context): Float {
        if (textSize == -1f) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val key = context.getString(R.string.pref_text_size)
            val value = sharedPrefs.getString(key, context.getString(R.string.text_size_medium_value))
            val dimenId = when (value) {
                context.getString(R.string.text_size_xsmall_value) -> R.dimen.text_size_xsmall
                context.getString(R.string.text_size_small_value) -> R.dimen.text_size_small
                context.getString(R.string.text_size_large_value) -> R.dimen.text_size_large
                context.getString(R.string.text_size_xlarge_value) -> R.dimen.text_size_xlarge
                else -> R.dimen.text_size_medium
            }
            textSize = context.resources.getDimension(dimenId)
        }
        return textSize
    }

    @JvmStatic fun getDefaultLogLevelPreference(context: Context): Char {
        if (defaultLogLevel == null) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val key = context.getString(R.string.pref_default_log_level)
            defaultLogLevel = sharedPrefs.getString(key, "V")!![0]
        }
        return defaultLogLevel!!
    }

    @JvmStatic fun getColorScheme(context: Context): ColorScheme = ColorScheme.Default
    @JvmStatic fun getThemeMode(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(THEME_MODE_KEY, THEME_SYSTEM)
    }
    @JvmStatic fun setThemeMode(context: Context, mode: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(THEME_MODE_KEY, mode).apply()
    }
    @JvmStatic fun setJellybeanRootRan(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(context.getString(R.string.pref_ran_jellybean_su_update), true).apply()
    }
    @JvmStatic fun getWidgetExistsPreference(context: Context, id: Int): Boolean = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(WIDGET_EXISTS_PREFIX + id, false)
    @JvmStatic fun setWidgetExistsPreference(context: Context, ids: IntArray) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        ids.forEach { editor.putBoolean(WIDGET_EXISTS_PREFIX + it, true) }
        editor.apply()
    }
    @JvmStatic fun clearWidgetExistsPreference(context: Context, id: Int) = PreferenceManager.getDefaultSharedPreferences(context).edit().remove(WIDGET_EXISTS_PREFIX + id).apply()
    @JvmStatic fun remapWidgetExistsPreference(context: Context, old: Int, new: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(WIDGET_EXISTS_PREFIX + new, true).remove(WIDGET_EXISTS_PREFIX + old).apply()
    }
    @JvmStatic fun getLogLinePeriodPreference(context: Context): Int = 200
    @JvmStatic fun isScrubberEnabled(context: Context): Boolean = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("scrubber", false)
    @JvmStatic fun getExpandedByDefaultPreference(context: Context): Boolean = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_expanded_by_default), false)
}
