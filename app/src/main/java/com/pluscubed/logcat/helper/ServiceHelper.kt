package com.pluscubed.logcat.helper

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.pluscubed.logcat.CrazyLoggerService
import com.pluscubed.logcat.LogcatRecordingService
import com.pluscubed.logcat.reader.LogcatReaderLoader
import com.pluscubed.logcat.util.UtilLogger

object ServiceHelper {
    private val log = UtilLogger(ServiceHelper::class.java)

    @JvmStatic
    fun startOrStopCrazyLogger(context: Context) {
        val alreadyRunning = checkIfServiceIsRunning(context, CrazyLoggerService::class.java)
        val intent = Intent(context, CrazyLoggerService::class.java)
        if (!alreadyRunning) {
            context.startService(intent)
        } else {
            context.stopService(intent)
        }
    }

    @JvmStatic
    @Synchronized
    fun stopBackgroundServiceIfRunning(context: Context) {
        val alreadyRunning = checkIfServiceIsRunning(context, LogcatRecordingService::class.java)
        log.d("Is CatlogService running: %s", alreadyRunning)
        if (alreadyRunning) {
            val intent = Intent(context, LogcatRecordingService::class.java)
            context.stopService(intent)
        }
    }

    @JvmStatic
    @Synchronized
    fun startBackgroundServiceIfNotAlreadyRunning(
        context: Context, filename: String?, queryFilter: String?, level: String?
    ) {
        val alreadyRunning = checkIfServiceIsRunning(context, LogcatRecordingService::class.java)
        log.d("Is CatlogService already running: %s", alreadyRunning)
        if (!alreadyRunning) {
            val intent = Intent(context, LogcatRecordingService::class.java)
            intent.putExtra(LogcatRecordingService.EXTRA_FILENAME, filename)
            val loader = LogcatReaderLoader.create(context, true)
            intent.putExtra(LogcatRecordingService.EXTRA_LOADER, loader)
            intent.putExtra(LogcatRecordingService.EXTRA_QUERY_FILTER, queryFilter)
            intent.putExtra(LogcatRecordingService.EXTRA_LEVEL, level)
            context.startService(intent)
        }
    }

    @JvmStatic
    fun checkIfServiceIsRunning(context: Context, service: Class<*>): Boolean {
        val serviceName = service.name
        val componentName = ComponentName(context.packageName, serviceName)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val procList = activityManager.getRunningServices(Int.MAX_VALUE)
        if (procList != null) {
            for (appProcInfo in procList) {
                if (appProcInfo != null && componentName == appProcInfo.service) {
                    log.d("%s is already running", serviceName)
                    return true
                }
            }
        }
        log.d("%s is not running", serviceName)
        return false
    }
}
