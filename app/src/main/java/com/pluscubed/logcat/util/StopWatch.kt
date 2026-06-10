package com.pluscubed.logcat.util

class StopWatch(private val name: String) {
    private var startTime: Long = 0

    init {
        if (UtilLogger.DEBUG_MODE) {
            this.startTime = System.currentTimeMillis()
        }
    }

    fun log(log: UtilLogger) {
        if (UtilLogger.DEBUG_MODE) {
            log.d("%s took %d ms", name, System.currentTimeMillis() - startTime)
        }
    }
}
