package com.pluscubed.logcat.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.pluscubed.logcat.data.ProcessInfo;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProcessInfoResolver {

    private static final long ACTIVITY_MANAGER_REFRESH_MS = 5_000;
    private static final long ROOT_PS_REFRESH_MS = 30_000;
    private static final ProcessInfoResolver INSTANCE = new ProcessInfoResolver();
    private static final UtilLogger log = new UtilLogger(ProcessInfoResolver.class);

    private final Object lock = new Object();
    private final Map<Integer, ProcessInfo> processMap = new HashMap<>();
    private long lastActivityManagerRefresh;
    private long lastRootPsRefresh;
    private boolean refreshRunning;

    private ProcessInfoResolver() {
    }

    public static ProcessInfoResolver getInstance() {
        return INSTANCE;
    }

    public ProcessInfo resolve(Context context, int pid) {
        if (pid <= 0) {
            return ProcessInfo.UNKNOWN;
        }

        refreshIfNeeded(context.getApplicationContext());

        synchronized (lock) {
            ProcessInfo info = processMap.get(pid);
            return info != null ? info : new ProcessInfo(pid, "", "", "", "", -1);
        }
    }

    private void refreshIfNeeded(final Context context) {
        long now = System.currentTimeMillis();
        synchronized (lock) {
            boolean activityManagerStale = now - lastActivityManagerRefresh >= ACTIVITY_MANAGER_REFRESH_MS;
            boolean rootPsStale = now - lastRootPsRefresh >= ROOT_PS_REFRESH_MS;
            if (refreshRunning || (!activityManagerStale && !rootPsStale)) {
                return;
            }
            refreshRunning = true;
            if (activityManagerStale) {
                lastActivityManagerRefresh = now;
            }
            if (rootPsStale) {
                lastRootPsRefresh = now;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<Integer, ProcessInfo> snapshot = new HashMap<>();
                    snapshot.putAll(readActivityManager(context));
                    for (Map.Entry<Integer, ProcessInfo> entry : readRootPs().entrySet()) {
                        snapshot.put(entry.getKey(), merge(snapshot.get(entry.getKey()), entry.getValue()));
                    }
                    synchronized (lock) {
                        for (Map.Entry<Integer, ProcessInfo> entry : snapshot.entrySet()) {
                            ProcessInfo previous = processMap.get(entry.getKey());
                            processMap.put(entry.getKey(), merge(previous, entry.getValue()));
                        }
                    }
                } finally {
                    synchronized (lock) {
                        refreshRunning = false;
                    }
                }
            }
        }, "DaoLogcatProcessInfo").start();
    }

    private Map<Integer, ProcessInfo> readActivityManager(Context context) {
        Map<Integer, ProcessInfo> result = new HashMap<>();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return result;
        }

        List<ActivityManager.RunningAppProcessInfo> processes =
                activityManager.getRunningAppProcesses();
        if (processes == null) {
            return result;
        }

        PackageManager packageManager = context.getPackageManager();
        for (ActivityManager.RunningAppProcessInfo process : processes) {
            String packageName = firstPackageName(process.pkgList,
                    packageManager.getPackagesForUid(process.uid));
            result.put(process.pid, new ProcessInfo(process.pid, process.processName, packageName,
                    "", "", process.uid));
        }
        return result;
    }

    private Map<Integer, ProcessInfo> readRootPs() {
        Map<Integer, ProcessInfo> result = new HashMap<>();
        Process process = null;
        PrintStream outputStream = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = new PrintStream(new BufferedOutputStream(process.getOutputStream(), 8192));
            outputStream.println("ps -AZ");
            outputStream.println("exit");
            outputStream.flush();
            outputStream.close();
            outputStream = null;

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 8192);
            String line;
            while ((line = reader.readLine()) != null) {
                ProcessInfo info = parsePsAzLine(line);
                if (info != null) {
                    result.put(info.getPid(), info);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.w(e, "Cannot refresh process list from root ps");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    private static ProcessInfo parsePsAzLine(String line) {
        if (TextUtils.isEmpty(line) || line.startsWith("LABEL ")) {
            return null;
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length < 4) {
            return null;
        }

        int pid;
        try {
            pid = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        StringBuilder name = new StringBuilder(parts[parts.length - 1]);
        return new ProcessInfo(pid, name.toString(), "", parts[1], parts[0], -1);
    }

    private static ProcessInfo merge(ProcessInfo previous, ProcessInfo current) {
        if (previous == null) {
            return current;
        }

        return new ProcessInfo(
                current.getPid() > 0 ? current.getPid() : previous.getPid(),
                nonEmpty(current.getProcessName(), previous.getProcessName()),
                nonEmpty(current.getPackageName(), previous.getPackageName()),
                nonEmpty(current.getUser(), previous.getUser()),
                nonEmpty(current.getSelinuxLabel(), previous.getSelinuxLabel()),
                current.getUid() >= 0 ? current.getUid() : previous.getUid());
    }

    private static String nonEmpty(String preferred, String fallback) {
        return !TextUtils.isEmpty(preferred) ? preferred : fallback;
    }

    private static String firstPackageName(String[] pkgList, String[] uidPackages) {
        if (pkgList != null && pkgList.length > 0) {
            return pkgList[0];
        }
        if (uidPackages != null && uidPackages.length > 0) {
            return uidPackages[0];
        }
        return "";
    }
}
