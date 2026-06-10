package com.pluscubed.logcat.helper

import android.content.Context
import android.os.Environment
import com.pluscubed.logcat.data.SavedLog
import com.pluscubed.logcat.util.UtilLogger
import java.io.*
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SaveLogHelper {
    private val log = UtilLogger(SaveLogHelper::class.java)
    private const val BUFFER = 0x1000
    private const val CATLOG_DIR = "matlog"
    private const val SAVED_LOGS_DIR = "saved_logs"
    private const val TMP_DIR = "tmp"
    const val TEMP_LOG_FILENAME = "logcat.txt"
    const val TEMP_DEVICE_INFO_FILENAME = "device_info.txt"
    const val TEMP_DMESG_FILENAME = "dmesg.txt"
    const val TEMP_ZIP_FILENAME = "logcat"

    @JvmStatic fun getTempDirectory(): File = File(getCatlogDirectory(), TMP_DIR).also { if (!it.exists()) it.mkdirs() }
    @JvmStatic fun getSavedLogsDirectory(): File = File(getCatlogDirectory(), SAVED_LOGS_DIR).also { if (!it.exists()) it.mkdirs() }
    @JvmStatic fun getCatlogDirectory(): File = File(Environment.getExternalStorageDirectory(), CATLOG_DIR).also { if (!it.exists()) it.mkdirs() }

    @JvmStatic fun saveTemporaryFile(context: Context, filename: String, text: String?, lines: List<CharSequence>?): File? {
        return try {
            val tempFile = File(getTempDirectory(), filename)
            PrintStream(BufferedOutputStream(FileOutputStream(tempFile, false), BUFFER)).use { out ->
                if (text != null) out.print(text) else lines?.forEach { out.println(it) }
            }
            tempFile
        } catch (e: Exception) { null }
    }

    @JvmStatic fun getFile(filename: String): File = File(getSavedLogsDirectory(), filename)
    @JvmStatic fun deleteLogIfExists(filename: String) { File(getSavedLogsDirectory(), filename).takeIf { it.exists() }?.delete() }
    @JvmStatic fun getLastLogLine(buffer: String): String? {
        return try {
            val process = RuntimeHelper.exec(listOf("logcat", "-v", "time", "-b", buffer, "-d"))
            BufferedReader(InputStreamReader(process.inputStream), 8192).use { reader ->
                var last: String? = null
                var line: String?
                while (reader.readLine().also { line = it } != null) { last = line }
                last
            }
        } catch (e: Exception) { null }
    }

    @JvmStatic fun saveLog(text: CharSequence, filename: String): Boolean {
        return try {
            val file = File(getSavedLogsDirectory(), filename)
            PrintStream(BufferedOutputStream(FileOutputStream(file, true), BUFFER)).use { out ->
                out.println(text)
            }
            true
        } catch (e: Exception) { false }
    }

    @JvmStatic fun openLog(filename: String, maxLines: Int): SavedLog {
        val logFile = File(getSavedLogsDirectory(), filename)
        val logLines = LinkedList<String>()
        var truncated = false
        try {
            BufferedReader(InputStreamReader(FileInputStream(logFile)), BUFFER).use { reader ->
                while (reader.ready()) {
                    logLines.add(reader.readLine() ?: break)
                    if (logLines.size > maxLines) { logLines.removeFirst(); truncated = true }
                }
            }
        } catch (e: Exception) { }
        return SavedLog(logLines, truncated)
    }

    @JvmStatic fun createLogFilename(withDate: Boolean = true): String {
        if (!withDate) return "$TEMP_ZIP_FILENAME.zip"
        val now = Calendar.getInstance()
        val fmt = DecimalFormat("00")
        val year = DecimalFormat("0000").format(now.get(Calendar.YEAR))
        return "$TEMP_ZIP_FILENAME-$year-${fmt.format(now.get(Calendar.MONTH)+1)}-${fmt.format(now.get(Calendar.DAY_OF_MONTH))}-${fmt.format(now.get(Calendar.HOUR_OF_DAY))}-${fmt.format(now.get(Calendar.MINUTE))}-${fmt.format(now.get(Calendar.SECOND))}.zip"
    }

    @JvmStatic fun cleanTemp() { getTempDirectory().listFiles()?.forEach { it.delete() } }

    @JvmStatic fun saveZipFile(filename: String, files: List<File>): File? {
        return try {
            val zipFile = File(getSavedLogsDirectory(), filename)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile), BUFFER)).use { out ->
                files.forEach { file ->
                    out.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(out) }
                }
            }
            zipFile
        } catch (e: Exception) { null }
    }
}
