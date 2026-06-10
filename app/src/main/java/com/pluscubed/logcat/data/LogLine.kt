package com.pluscubed.logcat.data


import com.pluscubed.logcat.reader.ScrubberUtils
import com.pluscubed.logcat.util.LogLineAdapterUtil
import com.pluscubed.logcat.util.UtilLogger
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

class LogLine {
    val stableId: Long = nextStableId.incrementAndGet()
    var logLevel: Int = 0
    var tag: String? = null
    private var _logOutput: String? = null
    var logOutput: String?
        get() = _logOutput
        set(value) {
            _logOutput = if (isScrubberEnabled && value != null) {
                ScrubberUtils.scrubLine(value)
            } else {
                value
            }
            refreshClassification()
        }
    var processId: Int = -1
    var timestamp: String? = null
    var isExpanded: Boolean = false
    var isHighlighted: Boolean = false
    var classification: LogClassification = LogClassification.NONE
        private set

    companion object {
        private const val TIMESTAMP_LENGTH = 19
        private val logPattern = Pattern.compile(
            // log level
            "([A-Z])/" +
                    // tag
                    "([^(]+)" +
                    "\\(\\s*" +
                    // pid
                    "(\\d+)" +
                    // optional weird number that only occurs on ZTE blade
                    "(?:\\*\\s*\\d+)?" +
                    "\\): "
        )

        private val log = UtilLogger(LogLine::class.java)
        private val nextStableId = AtomicLong()
        var isScrubberEnabled = false

        @JvmStatic
        fun newLogLine(originalLine: String, expanded: Boolean): LogLine {
            val logLine = LogLine()
            logLine.isExpanded = expanded

            var startIdx = 0

            if (originalLine.isNotEmpty() &&
                Character.isDigit(originalLine[0]) &&
                originalLine.length >= TIMESTAMP_LENGTH
            ) {
                val timestamp = originalLine.substring(0, TIMESTAMP_LENGTH - 1)
                logLine.timestamp = timestamp
                startIdx = TIMESTAMP_LENGTH
            }

            val matcher = logPattern.matcher(originalLine)

            if (matcher.find(startIdx)) {
                val logLevelChar = matcher.group(1)!![0]

                logLine.logLevel = convertCharToLogLevel(logLevelChar)
                logLine.tag = matcher.group(2)
                logLine.processId = matcher.group(3)!!.toInt()

                logLine.logOutput = originalLine.substring(matcher.end())
            } else {
                log.d("Line doesn't match pattern: %s", originalLine)
                logLine.logOutput = originalLine
                logLine.logLevel = -1
            }

            return logLine
        }

        @JvmStatic
        fun newFillerLine(): LogLine {
            val logLine = LogLine()
            logLine.logOutput = ""
            logLine.logLevel = -1
            return logLine
        }

        private fun convertCharToLogLevel(logLevelChar: Char): Int {
            return when (logLevelChar) {
                'D' -> 3
                'E' -> 6
                'I' -> 4
                'V' -> 2
                'W' -> 5
                'F' -> LogLineAdapterUtil.LOG_WTF
                else -> -1
            }
        }

        private fun convertLogLevelToChar(logLevel: Int): Char {
            return when (logLevel) {
                3 -> 'D'
                6 -> 'E'
                4 -> 'I'
                2 -> 'V'
                5 -> 'W'
                LogLineAdapterUtil.LOG_WTF -> 'F'
                else -> ' '
            }
        }
    }

    fun getOriginalLine(): String {
        if (logLevel == -1) {
            return logOutput ?: ""
        }

        val stringBuilder = StringBuilder()

        if (timestamp != null) {
            stringBuilder.append(timestamp).append(' ')
        }

        stringBuilder.append(convertLogLevelToChar(logLevel))
            .append('/')
            .append(tag)
            .append('(')
            .append(processId)
            .append("): ")
            .append(logOutput)

        return stringBuilder.toString()
    }

    fun getProcessIdText(): String {
        return convertLogLevelToChar(logLevel).toString()
    }

    fun isFillerLine(): Boolean {
        return (logOutput?.length ?: 0) == 0 && logLevel == -1
    }

    private fun refreshClassification() {
        classification = LogClassification.from(this)
    }
}
