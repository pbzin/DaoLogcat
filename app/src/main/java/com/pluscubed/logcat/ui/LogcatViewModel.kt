package com.pluscubed.logcat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.helper.PreferenceHelper
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

    private var reader: LogcatReader? = null
    private var isPaused = false
    private var currentLines = mutableListOf<LogLine>()
    private var readerJob: Job? = null

    fun startLogcat() {
        if (readerJob?.isActive == true) return // Already running

        readerJob = viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val loader = LogcatReaderLoader.create(getApplication(), true)
                    reader = loader.loadReader()
                    val maxLines = PreferenceHelper.getDisplayLimitPreference(getApplication())

                    // Batching mechanism
                    launch {
                        while (isActive) {
                            if (!isPaused && currentLines.isNotEmpty()) {
                                synchronized(currentLines) {
                                    _logLines.value = currentLines.toList()
                                }
                                _isLoading.value = false
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
                } finally {
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
        synchronized(currentLines) {
            currentLines.clear()
        }
        _logLines.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopReader()
        readerJob?.cancel()
    }
}
