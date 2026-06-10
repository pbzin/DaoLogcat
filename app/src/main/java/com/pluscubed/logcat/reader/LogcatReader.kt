package com.pluscubed.logcat.reader
import java.io.IOException
interface LogcatReader {
    @Throws(IOException::class)
    fun readLine(): String?
    fun killQuietly()
    fun readyToRecord(): Boolean
    fun getProcesses(): List<Process>
}
