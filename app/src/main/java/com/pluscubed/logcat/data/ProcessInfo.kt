package com.pluscubed.logcat.data

import android.text.TextUtils

class ProcessInfo(
    val pid: Int,
    processName: String?,
    packageName: String?,
    user: String?,
    selinuxLabel: String?,
    val uid: Int
) {
    val processName: String = processName ?: ""
    val packageName: String = packageName ?: ""
    val user: String = user ?: ""
    val selinuxLabel: String = selinuxLabel ?: ""

    companion object {
        @JvmField
        val UNKNOWN = ProcessInfo(-1, "", "", "", "", -1)

        private fun containsAny(text: String?, vararg needles: String): Boolean {
            if (TextUtils.isEmpty(text)) {
                return false
            }
            for (needle in needles) {
                if (text!!.contains(needle)) {
                    return true
                }
            }
            return false
        }
    }

    fun isKnown(): Boolean {
        return pid > 0 && (processName.isNotEmpty()
                || packageName.isNotEmpty()
                || selinuxLabel.isNotEmpty())
    }

    fun isAppProcess(): Boolean {
        return containsAny(
            selinuxLabel, "untrusted_app", "isolated_app", "gmscore_app",
            "priv_app", "platform_app"
        )
                || user.startsWith("u0_a")
                || user.startsWith("u:r:untrusted_app")
                || (packageName.isNotEmpty() && uid >= 10000)
    }

    fun isVendorProcess(): Boolean {
        return containsAny(selinuxLabel, "vendor_", "hal_", "qti", "qcom")
                || processName.startsWith("vendor.")
                || processName.contains("vendor/")
                || processName.contains("qti")
                || processName.contains("qcom")
    }

    fun isSystemProcess(): Boolean {
        return containsAny(
            selinuxLabel, "system_server", "system_app", "surfaceflinger",
            "audioserver", "mediaserver", "mediacodec", "cameraserver"
        )
                || containsAny(
            processName, "system_server", "surfaceflinger", "audioserver",
            "media.codec", "media.extractor", "cameraserver"
        )
    }

    fun getDisplayName(): String {
        if (packageName.isNotEmpty()) {
            return packageName
        }
        if (processName.isNotEmpty()) {
            return processName
        }
        return if (pid > 0) pid.toString() else ""
    }

    fun getCompactName(): String {
        val displayName = getDisplayName()
        if (displayName.isEmpty()) {
            return ""
        }
        if (displayName.length <= 28) {
            return displayName
        }
        return displayName.substring(0, 12) + "..." + displayName.substring(displayName.length - 12)
    }
}
