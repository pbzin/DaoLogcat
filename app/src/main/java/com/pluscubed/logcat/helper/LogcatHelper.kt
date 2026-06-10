package com.pluscubed.logcat.helper

import com.pluscubed.logcat.util.UtilLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object LogcatHelper {
    const val BUFFER_MAIN = "main"
    const val BUFFER_EVENTS = "events"
    const val BUFFER_RADIO = "radio"
    const val BUFFER_ALL = "all"
    private val log = UtilLogger(LogcatHelper::class.java)

    @JvmStatic
    @Throws(IOException::class)
    fun getLogcatProcess(buffer: String?): Process {
        val args = getLogcatArgs(buffer)
        return RuntimeHelper.exec(args)
    }

    private fun getLogcatArgs(buffer: String?): MutableList<String> {
        val args = mutableListOf("logcat", "-v", "time")
        if (BUFFER_ALL == buffer) {
            args.add("-b")
            args.add(BUFFER_ALL)
        } else if (BUFFER_MAIN != buffer && buffer != null) {
            args.add("-b")
            args.add(buffer)
        }
        return args
    }

    @JvmStatic
    fun getLastLogLine(buffer: String?): String? {
        var dumpLogcatProcess: Process? = null
        var reader: BufferedReader? = null
        var result: String? = null
        try {
            val args = getLogcatArgs(buffer)
            args.add("-d")
            dumpLogcatProcess = RuntimeHelper.exec(args)
            reader = BufferedReader(InputStreamReader(dumpLogcatProcess.inputStream), 8192)
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result = line
            }
        } catch (e: IOException) {
            log.e(e, "unexpected exception")
        } finally {
            if (dumpLogcatProcess != null) {
                RuntimeHelper.destroy(dumpLogcatProcess)
                log.d("destroyed 1 dump logcat process")
            }
            if (VersionHelper.getVersionSdkIntCompat() < VersionHelper.VERSION_JELLYBEAN
                && reader != null
            ) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    log.e(e, "unexpected exception")
                }
            }
        }
        return result
    }
}
