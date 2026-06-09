package com.pluscubed.logcat.data;

import android.text.TextUtils;

public final class ProcessInfo {

    public static final ProcessInfo UNKNOWN = new ProcessInfo(-1, "", "", "", "", -1);

    private final int pid;
    private final String processName;
    private final String packageName;
    private final String user;
    private final String selinuxLabel;
    private final int uid;

    public ProcessInfo(int pid, String processName, String packageName, String user,
                       String selinuxLabel, int uid) {
        this.pid = pid;
        this.processName = processName != null ? processName : "";
        this.packageName = packageName != null ? packageName : "";
        this.user = user != null ? user : "";
        this.selinuxLabel = selinuxLabel != null ? selinuxLabel : "";
        this.uid = uid;
    }

    public int getPid() {
        return pid;
    }

    public String getProcessName() {
        return processName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getUser() {
        return user;
    }

    public String getSelinuxLabel() {
        return selinuxLabel;
    }

    public int getUid() {
        return uid;
    }

    public boolean isKnown() {
        return pid > 0 && (!TextUtils.isEmpty(processName)
                || !TextUtils.isEmpty(packageName)
                || !TextUtils.isEmpty(selinuxLabel));
    }

    public boolean isAppProcess() {
        return containsAny(selinuxLabel, "untrusted_app", "isolated_app", "gmscore_app",
                "priv_app", "platform_app")
                || user.startsWith("u0_a")
                || user.startsWith("u:r:untrusted_app")
                || (!TextUtils.isEmpty(packageName) && uid >= 10000);
    }

    public boolean isVendorProcess() {
        return containsAny(selinuxLabel, "vendor_", "hal_", "qti", "qcom")
                || processName.startsWith("vendor.")
                || processName.contains("vendor/")
                || processName.contains("qti")
                || processName.contains("qcom");
    }

    public boolean isSystemProcess() {
        return containsAny(selinuxLabel, "system_server", "system_app", "surfaceflinger",
                "audioserver", "mediaserver", "mediacodec", "cameraserver")
                || containsAny(processName, "system_server", "surfaceflinger", "audioserver",
                "media.codec", "media.extractor", "cameraserver");
    }

    public String getDisplayName() {
        if (!TextUtils.isEmpty(packageName)) {
            return packageName;
        }
        if (!TextUtils.isEmpty(processName)) {
            return processName;
        }
        return pid > 0 ? String.valueOf(pid) : "";
    }

    public String getCompactName() {
        String displayName = getDisplayName();
        if (TextUtils.isEmpty(displayName)) {
            return "";
        }
        if (displayName.length() <= 28) {
            return displayName;
        }
        return displayName.substring(0, 12) + "..." + displayName.substring(displayName.length() - 12);
    }

    private static boolean containsAny(String text, String... needles) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
