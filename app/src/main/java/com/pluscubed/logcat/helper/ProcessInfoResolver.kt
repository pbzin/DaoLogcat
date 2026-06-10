package com.pluscubed.logcat.helper

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import com.pluscubed.logcat.data.ProcessInfo
import com.pluscubed.logcat.util.UtilLogger
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.util.HashMap

class ProcessInfoResolver private constructor() {
    private val lock = Any()
    private val processMap: MutableMap<Int, ProcessInfo> = HashMap()
    private var lastActivityManagerRefresh: Long = 0
    private var lastRootPsRefresh: Long = 0
    private var refreshRunning = false

    fun resolve(context: Context, pid: Int): ProcessInfo {
        if (pid <= 0) {
            return ProcessInfo.UNKNOWN
        }
        refreshIfNeeded(context.applicationContext)
        synchronized(lock) {
            val info = processMap[pid]
            return info ?: ProcessInfo(pid, "", "", "", "", -1)
        }
    }

    private fun refreshIfNeeded(context: Context) {
        val now = System.currentTimeMillis()
        synchronized(lock) {
            val activityManagerStale = now - lastActivityManagerRefresh >= ACTIVITY_MANAGER_REFRESH_MS
            val rootPsStale = now - lastRootPsRefresh >= ROOT_PS_REFRESH_MS
            if (refreshRunning || (!activityManagerStale && !rootPsStale)) {
                return
            }
            refreshRunning = true
            if (activityManagerStale) {
                lastActivityManagerRefresh = now
            }
            if (rootPsStale) {
                lastRootPsRefresh = now
            }
        }

        Thread({
            try {
                val snapshot: MutableMap<Int, ProcessInfo> = HashMap()
                snapshot.putAll(readActivityManager(context))
                for ((key, value) in readRootPs()) {
                    snapshot[key] = merge(snapshot[key], value)
                }
                synchronized(lock) {
                    for ((key, value) in snapshot) {
                        val previous = processMap[key]
                        processMap[key] = merge(previous, value)
                    }
                }
            } finally {
                synchronized(lock) {
                    refreshRunning = false
                }
            }
        }, "DaoLogcatProcessInfo").start()
    }

    private fun readActivityManager(context: Context): Map<Int, ProcessInfo> {
        val result: MutableMap<Int, ProcessInfo> = HashMap()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            ?: return result
        val processes = activityManager.runningAppProcesses ?: return result
        val packageManager = context.packageManager
        for (process in processes) {
            val packageName = firstPackageName(
                process.pkgList,
                packageManager.getPackagesForUid(process.uid)
            )
            result[process.pid] = ProcessInfo(
                process.pid, process.processName, packageName,
                "", "", process.uid
            )
        }
        return result
    }

    private fun readRootPs(): Map<Int, ProcessInfo> {
        val result: MutableMap<Int, ProcessInfo> = HashMap()
        var process: Process? = null
        var outputStream: PrintStream? = null
        var reader: BufferedReader? = null
        try {
            process = Runtime.getRuntime().exec("su")
            outputStream = PrintStream(BufferedOutputStream(process.outputStream, 8192))
            outputStream.println("ps -AZ")
            outputStream.println("exit")
            outputStream.flush()
            outputStream.close()
            outputStream = null
            reader = BufferedReader(InputStreamReader(process.inputStream), 8192)
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val info = parsePsAzLine(line)
                if (info != null) {
                    result[info.pid] = info
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            log.w(e, "Cannot refresh process list from root ps")
        } finally {
            outputStream?.close()
            try {
                reader?.close()
            } catch (ignored: IOException) {
            }
            process?.destroy()
        }
        return result
    }

    private fun parsePsAzLine(line: String?): ProcessInfo? {
        if (TextUtils.isEmpty(line) || line!!.startsWith("LABEL ")) {
            return null
        }
        val parts = line.trim { it <= ' ' }.split("\\s+".toRegex()).toTypedArray()
        if (parts.size < 4) {
            return null
        }
        val pid: Int = try {
            parts[2].toInt()
        } catch (e: NumberFormatException) {
            return null
        }
        val name = parts[parts.size - 1]
        return ProcessInfo(pid, name, "", parts[1], parts[0], -1)
    }

    private fun merge(previous: ProcessInfo?, current: ProcessInfo): ProcessInfo {
        if (previous == null) {
            return current
        }
        return ProcessInfo(
            if (current.pid > 0) current.pid else previous.pid,
            nonEmpty(current.processName, previous.processName),
            nonEmpty(current.packageName, previous.packageName),
            nonEmpty(current.user, previous.user),
            nonEmpty(current.selinuxLabel, previous.selinuxLabel),
            if (current.uid >= 0) current.uid else previous.uid
        )
    }

    private fun nonEmpty(preferred: String?, fallback: String?): String? {
        return if (!TextUtils.isEmpty(preferred)) preferred else fallback
    }

    private fun firstPackageName(pkgList: Array<String>?, uidPackages: Array<String>?): String {
        if (pkgList != null && pkgList.isNotEmpty()) {
            return pkgList[0]
        }
        return if (uidPackages != null && uidPackages.isNotEmpty()) {
            uidPackages[0]
        } else ""
    }

    companion object {
        private const val ACTIVITY_MANAGER_REFRESH_MS = 5000L
        private const val ROOT_PS_REFRESH_MS = 30000L
        private val INSTANCE = ProcessInfoResolver()
        private val log = UtilLogger(ProcessInfoResolver::class.java)

        @JvmStatic
        fun getInstance(): ProcessInfoResolver {
            return INSTANCE
        }
    }
}
