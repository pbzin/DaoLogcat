package com.pluscubed.logcat.helper

import com.pluscubed.logcat.util.UtilLogger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object DmesgHelper {
    private val log = UtilLogger(DmesgHelper::class.java)

    private fun getDmesgArgs(): List<String> {
        return listOf("dmesg")
    }

    @JvmStatic
    fun getDmsg(): List<CharSequence> {
        var dmesgProcess: Process? = null
        var reader: BufferedReader? = null
        val lines = mutableListOf<CharSequence>()
        try {
            val args = getDmesgArgs()
            dmesgProcess = RuntimeHelper.exec(args)
            reader = BufferedReader(InputStreamReader(dmesgProcess.inputStream), 8192)
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                lines.add(line!!)
            }
        } catch (e: IOException) {
            log.e(e, "unexpected exception")
        } finally {
            if (dmesgProcess != null) {
                RuntimeHelper.destroy(dmesgProcess)
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
        return lines
    }
}
