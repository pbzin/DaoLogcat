package com.pluscubed.logcat.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.pluscubed.logcat.util.UtilLogger
import org.omnirom.logcat.R
import java.io.*
import java.util.regex.Pattern

object SuperUserHelper {
    private val log = UtilLogger(SuperUserHelper::class.java)
    private var failedToObtainRoot = false
    private val SPACES_PATTERN = Pattern.compile("\\s+")
    private val PID_PATTERN = Pattern.compile("\\d+")

    private fun showWarningDialog(context: Context) {
        if (context !is Activity) {
            return
        }
        Handler(Looper.getMainLooper()).post {
            AlertDialog.Builder(context)
                .setTitle(R.string.no_logs_warning_title)
                .setMessage(R.string.no_logs_warning)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun haveReadLogsPermission(context: Context): Boolean {
        return context.packageManager.checkPermission(
            "android.permission.READ_LOGS",
            context.packageName
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAllRelatedPids(pid: Int): List<Int> {
        val result = mutableListOf<Int>()
        result.add(pid)
        try {
            val suProcess = Runtime.getRuntime().exec("su")
            Thread {
                var outputStream: PrintStream? = null
                try {
                    outputStream = PrintStream(BufferedOutputStream(suProcess.outputStream, 8192))
                    outputStream.println("ps")
                    outputStream.println("exit")
                    outputStream.flush()
                } finally {
                    outputStream?.close()
                }
            }.start()

            suProcess.waitFor()

            BufferedReader(InputStreamReader(suProcess.inputStream), 8192).use { reader ->
                while (reader.ready()) {
                    val line = SPACES_PATTERN.split(reader.readLine())
                    if (line.size >= 3) {
                        try {
                            if (pid == line[2].toInt()) {
                                result.add(line[1].toInt())
                            }
                        } catch (ignore: NumberFormatException) {
                        }
                    }
                }
            }
        } catch (e1: IOException) {
            log.e(e1, "cannot get process ids")
        } catch (e2: InterruptedException) {
            log.e(e2, "cannot get pids")
        }
        return result
    }

    @JvmStatic
    fun destroy(process: Process) {
        val matcher = PID_PATTERN.matcher(process.toString())
        if (matcher.find()) {
            val pid = matcher.group().toInt()
            val allRelatedPids = getAllRelatedPids(pid)
            log.d("Killing %s", allRelatedPids)
            for (relatedPid in allRelatedPids) {
                destroyPid(relatedPid)
            }
        }
    }

    private fun destroyPid(pid: Int) {
        var suProcess: Process? = null
        var outputStream: PrintStream? = null
        try {
            suProcess = Runtime.getRuntime().exec("su")
            outputStream = PrintStream(BufferedOutputStream(suProcess.outputStream, 8192))
            outputStream.println("kill $pid")
            outputStream.println("exit")
            outputStream.flush()
        } catch (e: IOException) {
            log.e(e, "cannot kill process $pid")
        } finally {
            outputStream?.close()
            try {
                suProcess?.waitFor()
            } catch (e: InterruptedException) {
                log.e(e, "cannot kill process $pid")
            }
        }
    }

    @JvmStatic
    fun requestRoot(context: Context) {
        if (haveReadLogsPermission(context)) {
            failedToObtainRoot = false
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val toastRunnable = Runnable {
            Toast.makeText(context, R.string.toast_request_root, Toast.LENGTH_LONG).show()
        }
        handler.postDelayed(toastRunnable, 200)

        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("echo hello\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

            process.waitFor()
            if (process.exitValue() != 0) {
                showWarningDialog(context)
                failedToObtainRoot = true
            } else {
                failedToObtainRoot = false
                PreferenceHelper.setJellybeanRootRan(context)
            }
        } catch (e: Exception) {
            log.w(e, "Cannot obtain root")
            showWarningDialog(context)
            failedToObtainRoot = true
        }
        handler.removeCallbacks(toastRunnable)
    }

    @JvmStatic
    fun isFailedToObtainRoot(): Boolean = failedToObtainRoot
}
