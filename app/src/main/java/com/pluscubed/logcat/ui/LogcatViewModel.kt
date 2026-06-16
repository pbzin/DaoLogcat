package com.pluscubed.logcat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.helper.RuntimeHelper
import com.pluscubed.logcat.reader.LogcatReader
import com.pluscubed.logcat.reader.LogcatReaderLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LogcatViewModel(application: Application) : AndroidViewModel(application) {
    private val _logLines = MutableStateFlow<List<LogLine>>(emptyList())
    val logLines: StateFlow<List<LogLine>> = _logLines

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _status = MutableStateFlow("Starting root logcat")
    val status: StateFlow<String> = _status

    private val _filterQuery = MutableStateFlow("")
    val filterQuery: StateFlow<String> = _filterQuery

    private val _minLogLevel = MutableStateFlow(0)
    val minLogLevel: StateFlow<Int> = _minLogLevel

    private var reader: LogcatReader? = null
    private var isPaused = false
    private var currentLines = mutableListOf<LogLine>()
    private var readerJob: Job? = null

    fun startLogcat() {
        if (readerJob?.isActive == true) return // Already running

        readerJob = viewModelScope.launch {
            _isLoading.value = true
            _status.value = "Requesting root access"
            withContext(Dispatchers.IO) {
                try {
                    com.pluscubed.logcat.helper.SuperUserHelper.requestRoot(getApplication())
                    _status.value = "Opening full logcat buffers"
                    val loader = LogcatReaderLoader.create(getApplication(), false)
                    reader = loader.loadReader()
                    val maxLines = PreferenceHelper.getDisplayLimitPreference(getApplication())
                    _status.value = "Waiting for log lines"

                    // Batching mechanism
                    launch {
                        while (isActive) {
                            if (!isPaused && currentLines.isNotEmpty()) {
                                synchronized(currentLines) {
                                    _logLines.value = filteredLinesLocked()
                                }
                                _isLoading.value = false
                                _status.value = if (_filterQuery.value.isEmpty() && _minLogLevel.value == 0) {
                                    "Live full capture"
                                } else {
                                    "Filtered live capture"
                                }
                            }
                            delay(200) // Update UI at most every 200ms
                        }
                    }

                    while (isActive) {
                        val line = reader?.readLine() ?: break
                        if (isPaused) continue

                        val logLine = LogLine.newLogLine(line, false)
                        synchronized(currentLines) {
                            currentLines.add(logLine)
                            if (currentLines.size > maxLines) {
                                currentLines.removeAt(0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _status.value = e.message ?: "Failed to open logcat"
                } finally {
                    _isLoading.value = false
                    stopReader()
                }
            }
        }
    }

    private fun stopReader() {
        reader?.killQuietly()
        reader = null
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun clearLogs() {
        viewModelScope.launch {
            val jobToCancel = readerJob
            readerJob = null 
            jobToCancel?.cancel()
            stopReader()
            try {
                jobToCancel?.join()
            } catch (e: Exception) {}

            synchronized(currentLines) {
                currentLines.clear()
            }
            _logLines.value = emptyList()

            withContext(Dispatchers.IO) {
                try {
                    // Clears all buffers and waits for completion
                    RuntimeHelper.exec(listOf("logcat", "-b", "all", "-c")).waitFor()
                } catch (e: Exception) {
                    try {
                        RuntimeHelper.exec(listOf("logcat", "-c")).waitFor()
                    } catch (e2: Exception) {}
                }
                delay(300) // Wait a bit for the system to process the clearing
            }

            startLogcat()
        }
    }

    fun setFilter(query: String, minLevel: Int) {
        _filterQuery.value = query.trim()
        _minLogLevel.value = minLevel
        synchronized(currentLines) {
            _logLines.value = filteredLinesLocked()
        }
    }

    fun clearFilter() {
        setFilter("", 0)
    }

    fun exportVisibleLogText(): String {
        val lines = _logLines.value.asReversed()
        return buildString {
            appendLine("DaoLogcat snapshot")
            appendLine("Order: newest first")
            appendLine("Filter: ${activeFilterDescription()}")
            appendLine()
            lines.forEach { appendLine(it.getOriginalLine()) }
        }
    }

    private fun activeFilterDescription(): String {
        val level = when (_minLogLevel.value) {
            2 -> "Verbose and newer"
            3 -> "Debug and newer"
            4 -> "Info and newer"
            5 -> "Warnings and errors"
            6 -> "Errors only"
            else -> "All levels"
        }
        val query = _filterQuery.value
        return if (query.isEmpty()) level else "$level, text contains \"$query\""
    }

    private fun filteredLinesLocked(): List<LogLine> {
        val query = _filterQuery.value.lowercase()
        val minLevel = _minLogLevel.value
        return currentLines.filter { line ->
            val levelMatches = minLevel == 0 || line.logLevel >= minLevel
            val queryMatches = query.isEmpty()
                    || line.tag.orEmpty().lowercase().contains(query)
                    || line.logOutput.orEmpty().lowercase().contains(query)
                    || line.classification.label.lowercase().contains(query)
                    || line.classification.summary.lowercase().contains(query)
            levelMatches && queryMatches
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopReader()
        readerJob?.cancel()
    }
}
