package com.pluscubed.logcat.reader
import android.text.TextUtils
import com.pluscubed.logcat.helper.LogcatHelper
import com.pluscubed.logcat.helper.RuntimeHelper
import com.pluscubed.logcat.helper.VersionHelper
import com.pluscubed.logcat.util.UtilLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
class SingleLogcatReader(recordingMode: Boolean, val logBuffer: String?, private var lastLine: String?) : AbsLogcatReader(recordingMode) {
    private var logcatProcess: Process? = null
    private var bufferedReader: BufferedReader? = null
    init { init() }
    @Throws(IOException::class)
    private fun init() {
        logcatProcess = LogcatHelper.getLogcatProcess(logBuffer)
        bufferedReader = BufferedReader(InputStreamReader(logcatProcess!!.inputStream), 8192)
    }
    override fun killQuietly() {
        if (logcatProcess != null) { RuntimeHelper.destroy(logcatProcess!!) }
        if (VersionHelper.getVersionSdkIntCompat() < VersionHelper.VERSION_JELLYBEAN && bufferedReader != null) {
            try { bufferedReader!!.close() } catch (e: IOException) { }
        }
    }
    @Throws(IOException::class)
    override fun readLine(): String? {
        val line = bufferedReader!!.readLine()
        if (isRecordingMode && lastLine != null) { if (lastLine == line) { lastLine = null } }
        return line
    }
    override fun readyToRecord(): Boolean = isRecordingMode && lastLine == null
    override fun getProcesses(): List<Process> = listOfNotNull(logcatProcess)
}
