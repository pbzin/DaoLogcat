package com.pluscubed.logcat.helper

import android.content.Context
import android.content.pm.PackageManager
import com.pluscubed.logcat.util.UtilLogger

object PackageHelper {
    private val log = UtilLogger(PackageHelper::class.java)

    @JvmStatic
    fun isCatlogDonateInstalled(context: Context): Boolean {
        return true
    }

    @JvmStatic
    fun getVersionName(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            log.d(e, "unexpected exception")
            ""
        }
    }
}
