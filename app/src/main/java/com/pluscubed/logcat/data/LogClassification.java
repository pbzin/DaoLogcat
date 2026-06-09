package com.pluscubed.logcat.data;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.Locale;

public final class LogClassification {

    public static final LogClassification NONE =
            new LogClassification("", "Unclassified", Color.TRANSPARENT);

    private final String label;
    private final String summary;
    private final int color;

    private LogClassification(String label, String summary, int color) {
        this.label = label;
        this.summary = summary;
        this.color = color;
    }

    public static LogClassification from(LogLine line) {
        return from(line, ProcessInfo.UNKNOWN);
    }

    public static LogClassification from(LogLine line, ProcessInfo processInfo) {
        if (line == null || line.getTag() == null || line.getLogOutput() == null) {
            return NONE;
        }

        String tag = line.getTag().trim();
        String output = line.getLogOutput();
        String text = (tag + " " + output).toLowerCase(Locale.US);
        ProcessInfo process = processInfo != null ? processInfo : ProcessInfo.UNKNOWN;

        if (containsAny(text, "avc:  denied", "avc: denied")) {
            if (process.isAppProcess()) {
                return app("APP DENY", process, "App sandbox denial from resolved process.");
            }
            if (process.isVendorProcess() || process.isSystemProcess()) {
                return tree("SELINUX", process, "System/vendor SELinux denial; check sepolicy.");
            }
            return system("AVC", process, "SELinux denial; inspect source and target domains.");
        }

        if (containsAny(text, "dav1d", "meta.dav1d.av1.decoder", "exodav1d")) {
            if (process.isAppProcess()) {
                return app("APP AV1", process, "Resolved app process is using dav1d AV1 software decoder.");
            }
            return system("AV1 SW", process, "dav1d AV1 software decoder path; process origin unresolved.");
        }

        if (containsAny(text, "omx.qcom.video.decoder", "c2.qti.", "qc2v4l2codec", "qc2comp")) {
            return vendor("QCOM CODEC", process, "Qualcomm hardware codec path; correlate with media/gralloc errors.");
        }

        if (containsAny(text, "codec2", "mediacodec", "graphicbufferallocator",
                "bufferqueue", "surfaceflinger", "nativewindow")) {
            return system("MEDIA/SURFACE", process, "Framework media or surface path; correlate with app and HAL errors.");
        }

        if (containsAny(text, "acdb", "gsl_", "agm:", "agm ", "pal:", "pal ")) {
            if (containsAny(text, "graph_open", "graph_prepare", "graph_start",
                    "pal_stream_start", "pal_stream_close", "exit, ret 0", "status 0")) {
                return vendor("AUDIO OK", process, "Qualcomm audio path completed successfully.");
            }
            if (containsAny(text, "no calibration", "graph alias", "nonpersist", "delta ckv")) {
                return vendor("ACDB CAL", process, "Qualcomm audio calibration/alias path; correlate with graph result.");
            }
            return vendor("AUDIO HAL", process, "Qualcomm audio vendor path.");
        }

        if (containsAny(text, "vendor.camera-provider", "camera3-", "micameraalgo",
                "libxmi_", "cam_req_mgr", "qcamera")) {
            return vendor("CAMERA HAL", process, "Camera vendor/framework path; likely tree-relevant if paired with crash.");
        }

        if (containsAny(text, "igfunctional", "criticalpathmanager", "videoplayerimpl",
                "slowappcomponent", "unifiedtimeentrymerger", "litho_")) {
            if (process.isAppProcess()) {
                return app("APP TELEMETRY", process, "Resolved app process internal telemetry.");
            }
            return system("TELEMETRY", process, "Telemetry-like tag; process origin unresolved.");
        }

        if (containsAny(text, "msys", "getaddrinfo", "network_error", "failed to resolve")) {
            if (process.isAppProcess()) {
                return app("APP NETWORK", process, "Resolved app network resolver path.");
            }
            return system("NETWORK", process, "Network resolver path; inspect process origin.");
        }

        if (containsAny(text, "androidruntime", "fatal exception", "force finishing", "am_crash")) {
            return system("CRASH", process, "Crash signal; inspect package, stack, and process domain.");
        }

        if (process.isSystemProcess() || containsAny(tag.toLowerCase(Locale.US), "systemserver", "activitymanager",
                "windowmanager", "connectivityservice", "appops")) {
            return system("SYSTEM", process, "Android framework service log.");
        }

        if (process.isVendorProcess() || containsAny(text, "vendor.qti", "qti", "qcom", "qseecom", "keymaster",
                "keymint", "oemcrypto")) {
            return vendor("VENDOR", process, "Vendor/Qualcomm component; check blobs, props, VINTF, or sepolicy.");
        }

        if (process.isAppProcess()) {
            return app("APP", process, "Resolved app process log.");
        }

        if (process.isKnown()) {
            return system("PROCESS", process, "Resolved process log.");
        }

        return NONE;
    }

    public boolean hasLabel() {
        return !TextUtils.isEmpty(label);
    }

    public String getLabel() {
        return label;
    }

    public String getSummary() {
        return summary;
    }

    public int getColor() {
        return color;
    }

    private static LogClassification app(String label, ProcessInfo processInfo, String summary) {
        return new LogClassification(label, withProcess(processInfo, summary), Color.rgb(93, 64, 55));
    }

    private static LogClassification system(String label, ProcessInfo processInfo, String summary) {
        return new LogClassification(label, withProcess(processInfo, summary), Color.rgb(21, 101, 192));
    }

    private static LogClassification vendor(String label, ProcessInfo processInfo, String summary) {
        return new LogClassification(label, withProcess(processInfo, summary), Color.rgb(85, 139, 47));
    }

    private static LogClassification tree(String label, ProcessInfo processInfo, String summary) {
        return new LogClassification(label, withProcess(processInfo, summary), Color.rgb(198, 40, 40));
    }

    private static String withProcess(ProcessInfo processInfo, String summary) {
        if (processInfo == null || !processInfo.isKnown()) {
            return summary;
        }
        return processInfo.getDisplayName() + ": " + summary;
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
