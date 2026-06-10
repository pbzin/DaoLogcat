package com.pluscubed.logcat.helper

import android.os.Build
import java.lang.reflect.Field
import java.util.*

object BuildHelper {
    @JvmStatic
    fun getBuildInformationAsString(): String {
        val keysToValues: SortedMap<String, String> = TreeMap()
        putKeyValue(Build::class.java, "BOARD", keysToValues)
        putKeyValue(Build::class.java, "BOOTLOADER", keysToValues)
        putKeyValue(Build::class.java, "BRAND", keysToValues)
        putKeyValue(Build::class.java, "CPU_ABI", keysToValues)
        putKeyValue(Build::class.java, "CPU_ABI2", keysToValues)
        putKeyValue(Build::class.java, "DEVICE", keysToValues)
        putKeyValue(Build::class.java, "DISPLAY", keysToValues)
        putKeyValue(Build::class.java, "FINGERPRINT", keysToValues)
        putKeyValue(Build::class.java, "HARDWARE", keysToValues)
        putKeyValue(Build::class.java, "HOST", keysToValues)
        putKeyValue(Build::class.java, "ID", keysToValues)
        putKeyValue(Build::class.java, "MANUFACTURER", keysToValues)
        putKeyValue(Build::class.java, "MODEL", keysToValues)
        putKeyValue(Build::class.java, "PRODUCT", keysToValues)
        putKeyValue(Build::class.java, "RADIO", keysToValues)
        putKeyValue(Build::class.java, "TAGS", keysToValues)
        putKeyValue(Build::class.java, "TIME", keysToValues)
        putKeyValue(Build::class.java, "TYPE", keysToValues)
        putKeyValue(Build::class.java, "USER", keysToValues)
        putKeyValue(Build.VERSION::class.java, "CODENAME", keysToValues)
        putKeyValue(Build.VERSION::class.java, "INCREMENTAL", keysToValues)
        putKeyValue(Build.VERSION::class.java, "RELEASE", keysToValues)
        putKeyValue(Build.VERSION::class.java, "SDK_INT", keysToValues)

        val stringBuilder = StringBuilder()
        for ((key, value) in keysToValues) {
            stringBuilder.append(key).append(": ").append(value).append('\n')
        }
        return stringBuilder.toString()
    }

    private fun putKeyValue(clazz: Class<*>, buildField: String, keysToValues: SortedMap<String, String>) {
        try {
            val field: Field = clazz.getField(buildField)
            val value = field[null]
            val key = clazz.simpleName.lowercase(Locale.US) + "." + buildField.lowercase(Locale.US)
            keysToValues[key] = value.toString()
        } catch (e: Exception) {
            // ignore
        }
    }
}
