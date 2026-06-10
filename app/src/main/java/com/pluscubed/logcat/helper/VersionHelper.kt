package com.pluscubed.logcat.helper

import android.os.Build
import java.lang.reflect.Field

object VersionHelper {
    const val VERSION_CUPCAKE = 3
    const val VERSION_DONUT = 4
    const val VERSION_FROYO = 8
    const val VERSION_JELLYBEAN = 16

    private var sdkIntField: Field? = null
    private var fetchedSdkIntField = false

    @JvmStatic
    fun getVersionSdkIntCompat(): Int {
        try {
            val field = getSdkIntField()
            if (field != null) {
                return field[null] as Int
            }
        } catch (ignore: IllegalAccessException) {
        }
        return VERSION_CUPCAKE
    }

    private fun getSdkIntField(): Field? {
        if (!fetchedSdkIntField) {
            try {
                sdkIntField = Build.VERSION::class.java.getField("SDK_INT")
            } catch (ignore: NoSuchFieldException) {
            }
            fetchedSdkIntField = true
        }
        return sdkIntField
    }
}
