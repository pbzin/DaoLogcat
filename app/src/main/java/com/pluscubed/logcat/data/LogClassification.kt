package com.pluscubed.logcat.data

import android.graphics.Color
import java.util.Locale

class LogClassification private constructor(
    val label: String,
    val summary: String,
    val color: Int
) {
    companion object {
        @JvmField
        val NONE = LogClassification("", "Unclassified", Color.TRANSPARENT)

        @JvmStatic
        fun from(line: LogLine?): LogClassification {
            return from(line, ProcessInfo.UNKNOWN)
        }

        @JvmStatic
        fun from(line: LogLine?, processInfo: ProcessInfo?): LogClassification {
            if (line == null || line.tag == null || line.logOutput == null) {
                return NONE
            }

            val tag = line.tag!!.trim()
            val output = line.logOutput!!
            val text = (tag + " " + output).lowercase(Locale.US)
            val process = processInfo ?: ProcessInfo.UNKNOWN

            if (containsAny(text, "avc:  denied", "avc: denied")) {
                if (process.isAppProcess()) {
                    return app("APP DENY", process, "App sandbox denial from resolved process.")
                }
                if (process.isVendorProcess() || process.isSystemProcess()) {
                    return tree("SELINUX", process, "System/vendor SELinux denial; check sepolicy.")
                }
                return system("AVC", process, "SELinux denial; inspect source and target domains.")
            }

            if (containsAny(text, "dav1d", "meta.dav1d.av1.decoder", "exodav1d")) {
                if (process.isAppProcess()) {
                    return app("APP AV1", process, "Resolved app process is using dav1d AV1 software decoder.")
                }
                return system("AV1 SW", process, "dav1d AV1 software decoder path; process origin unresolved.")
            }

            if (containsAny(text, "omx.qcom.video.decoder", "c2.qti.", "qc2v4l2codec", "qc2comp")) {
                return vendor("QCOM CODEC", process, "Qualcomm hardware codec path; correlate with media/gralloc errors.")
            }

            if (containsAny(text, "codec2", "mediacodec", "graphicbufferallocator",
                    "bufferqueue", "surfaceflinger", "nativewindow")) {
                return system("MEDIA/SURFACE", process, "Framework media or surface path; correlate with app and HAL errors.")
            }

            if (containsAny(text, "acdb", "gsl_", "agm:", "agm ", "pal:", "pal ")) {
                if (containsAny(text, "graph_open", "graph_prepare", "graph_start",
                        "pal_stream_start", "pal_stream_close", "exit, ret 0", "status 0")) {
                    return vendor("AUDIO OK", process, "Qualcomm audio path completed successfully.")
                }
                if (containsAny(text, "no calibration", "graph alias", "nonpersist", "delta ckv")) {
                    return vendor("ACDB CAL", process, "Qualcomm audio calibration/alias path; correlate with graph result.")
                }
                return vendor("AUDIO HAL", process, "Qualcomm audio vendor path.")
            }

            if (containsAny(text, "vendor.camera-provider", "camera3-", "micameraalgo",
                    "libxmi_", "cam_req_mgr", "qcamera")) {
                return vendor("CAMERA HAL", process, "Camera vendor/framework path; likely tree-relevant if paired with crash.")
            }

            if (containsAny(text, "igfunctional", "criticalpathmanager", "videoplayerimpl",
                    "slowappcomponent", "unifiedtimeentrymerger", "litho_")) {
                if (process.isAppProcess()) {
                    return app("APP TELEMETRY", process, "Resolved app process internal telemetry.")
                }
                return system("TELEMETRY", process, "Telemetry-like tag; process origin unresolved.")
            }

            if (containsAny(text, "msys", "getaddrinfo", "network_error", "failed to resolve")) {
                if (process.isAppProcess()) {
                    return app("APP NETWORK", process, "Resolved app network resolver path.")
                }
                return system("NETWORK", process, "Network resolver path; inspect process origin.")
            }

            if (containsAny(text, "androidruntime", "fatal exception", "force finishing", "am_crash")) {
                return system("CRASH", process, "Crash signal; inspect package, stack, and process domain.")
            }

            if (process.isSystemProcess() || containsAny(tag.lowercase(Locale.US), "systemserver", "activitymanager",
                    "windowmanager", "connectivityservice", "appops")) {
                return system("SYSTEM", process, "Android framework service log.")
            }

            if (process.isVendorProcess() || containsAny(text, "vendor.qti", "qti", "qcom", "qseecom", "keymaster",
                    "keymint", "oemcrypto")) {
                return vendor("VENDOR", process, "Vendor/Qualcomm component; check blobs, props, VINTF, or sepolicy.")
            }

            if (process.isAppProcess()) {
                return app("APP", process, "Resolved app process log.")
            }

            if (process.isKnown()) {
                return system("PROCESS", process, "Resolved process log.")
            }

            return NONE
        }

        private fun app(label: String, processInfo: ProcessInfo, summary: String): LogClassification {
            return LogClassification(label, withProcess(processInfo, summary), Color.rgb(93, 64, 55))
        }

        private fun system(label: String, processInfo: ProcessInfo, summary: String): LogClassification {
            return LogClassification(label, withProcess(processInfo, summary), Color.rgb(21, 101, 192))
        }

        private fun vendor(label: String, processInfo: ProcessInfo, summary: String): LogClassification {
            return LogClassification(label, withProcess(processInfo, summary), Color.rgb(85, 139, 47))
        }

        private fun tree(label: String, processInfo: ProcessInfo, summary: String): LogClassification {
            return LogClassification(label, withProcess(processInfo, summary), Color.rgb(198, 40, 40))
        }

        private fun withProcess(processInfo: ProcessInfo?, summary: String): String {
            if (processInfo == null || !processInfo.isKnown()) {
                return summary
            }
            return processInfo.getDisplayName() + ": " + summary
        }

        private fun containsAny(text: String, vararg needles: String): Boolean {
            for (needle in needles) {
                if (text.contains(needle)) {
                    return true
                }
            }
            return false
        }
    }

    fun hasLabel(): Boolean {
        return label.isNotEmpty()
    }
}
