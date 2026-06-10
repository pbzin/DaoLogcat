package com.pluscubed.logcat.reader

import com.pluscubed.logcat.util.UtilLogger
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class MultipleLogcatReader(recordingMode: Boolean, lastLines: Map<String, String?>) : AbsLogcatReader(recordingMode) {
    private val readerThreads = mutableListOf<ReaderThread>()
    private val queue: BlockingQueue<String> = ArrayBlockingQueue(1)

    init {
        for ((logBuffer, lastLine) in lastLines) {
            val readerThread = ReaderThread(logBuffer, lastLine)
            readerThread.start()
            readerThreads.add(readerThread)
        }
    }

    @Throws(IOException::class)
    override fun readLine(): String? = try {
        val value = queue.take()
        if (value != "") value else null
    } catch (e: InterruptedException) {
        null
    }

    override fun readyToRecord(): Boolean = readerThreads.all { it.reader.readyToRecord() }

    override fun killQuietly() {
        for (thread in readerThreads) {
            thread.killed = true
        }
        // Run cleanup on a new thread to avoid blocking if needed, but not using GlobalScope
        Thread {
            for (thread in readerThreads) {
                thread.reader.killQuietly()
            }
            queue.offer("")
        }.start()
    }

    override fun getProcesses(): List<Process> = readerThreads.flatMap { it.reader.getProcesses() }

    private inner class ReaderThread(logBuffer: String, lastLine: String?) : Thread() {
        val reader = SingleLogcatReader(isRecordingMode, logBuffer, lastLine)
        @Volatile
        var killed = false

        override fun run() {
            try {
                while (!killed) {
                    val line = reader.readLine() ?: break
                    if (!killed) queue.put(line)
                }
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private val log = UtilLogger(MultipleLogcatReader::class.java)
    }
}
