package com.pluscubed.logcat

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.pluscubed.logcat.data.LogLine
import com.pluscubed.logcat.data.SearchCriteria
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.helper.SaveLogHelper
import com.pluscubed.logcat.helper.ServiceHelper
import com.pluscubed.logcat.helper.WidgetHelper
import com.pluscubed.logcat.reader.LogcatReader
import com.pluscubed.logcat.reader.LogcatReaderLoader
import com.pluscubed.logcat.ui.LogcatActivity
import com.pluscubed.logcat.util.ArrayUtil
import com.pluscubed.logcat.util.LogLineAdapterUtil
import com.pluscubed.logcat.util.UtilLogger
import org.omnirom.logcat.OmniApp
import org.omnirom.logcat.R
import java.io.IOException
import java.util.*

class LogcatRecordingService : IntentService("AppTrackerService") {
    private val lock = Any()
    private var mReader: LogcatReader? = null
    private var mKilled = false
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            log.d("onReceive()")
            killProcess()
            ServiceHelper.stopBackgroundServiceIfRunning(context)
        }
    }
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        log.d("onCreate()")
        val intentFilter = IntentFilter(ACTION_STOP_RECORDING)
        intentFilter.addDataScheme(URI_SCHEME)
        registerReceiver(receiver, intentFilter)
        handler = Handler(Looper.getMainLooper())
    }

    private fun initializeReader(intent: Intent) {
        try {
            val loader = intent.getParcelableExtra<LogcatReaderLoader>(EXTRA_LOADER)
            mReader = loader?.loadReader()
            while (mReader?.readyToRecord() == false && !mKilled) {
                mReader?.readLine()
            }
            if (!mKilled) {
                makeToast(R.string.log_recording_started, Toast.LENGTH_SHORT)
            }
        } catch (e: IOException) {
            log.d(e, "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log.d("onDestroy()")
        killProcess()
        unregisterReceiver(receiver)
        stopForeground(true)
        WidgetHelper.updateWidgets(applicationContext, false)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        log.d("onStart()")
        handleCommand()
    }

    private fun handleCommand() {
        WidgetHelper.updateWidgets(applicationContext)
        val tickerText = getText(R.string.notification_ticker)
        val stopRecordingIntent = Intent().apply {
            action = ACTION_STOP_RECORDING
            data = Uri.withAppendedPath(
                Uri.parse("$URI_SCHEME://stop/"),
                java.lang.Long.toHexString(Random().nextLong())
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, stopRecordingIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(applicationContext, OmniApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(tickerText)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_subtext))
            .setContentIntent(pendingIntent)
            .build()
        startForeground(R.string.notification_title, notification)
    }

    override fun onHandleIntent(intent: Intent?) {
        log.d("onHandleIntent()")
        if (intent != null) {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        log.d("Starting up %s now with intent: %s", LogcatRecordingService::class.java.simpleName, intent)
        val filename = intent.getStringExtra(EXTRA_FILENAME)
        val queryText = intent.getStringExtra(EXTRA_QUERY_FILTER)
        val logLevel = intent.getStringExtra(EXTRA_LEVEL)
        val searchCriteria = SearchCriteria(queryText)
        val logLevels = resources.getStringArray(R.array.log_levels_values)
        val logLevelLimit = ArrayUtil.indexOf(logLevels, logLevel!!)
        val searchCriteriaWillAlwaysMatch = searchCriteria.isEmpty()
        val logLevelAcceptsEverything = logLevelLimit == 0
        SaveLogHelper.deleteLogIfExists(filename!!)
        initializeReader(intent)
        val stringBuilder = StringBuilder()
        try {
            var line: String?
            var lineCount = 0
            val logLinePeriod = 200 // Hardcoded or from PreferenceHelper if added
            while (mReader?.readLine().also { line = it } != null && !mKilled) {
                if (!searchCriteriaWillAlwaysMatch || !logLevelAcceptsEverything) {
                    if (!checkLogLine(line!!, searchCriteria, logLevelLimit)) {
                        continue
                    }
                }
                stringBuilder.append(line).append("\n")
                if (++lineCount % logLinePeriod == 0) {
                    SaveLogHelper.saveLog(stringBuilder, filename)
                    stringBuilder.delete(0, stringBuilder.length)
                }
            }
        } catch (e: IOException) {
        } finally {
            killProcess()
            log.d("CatlogService ended")
            val logSaved = SaveLogHelper.saveLog(stringBuilder, filename)
            if (logSaved) {
                makeToast(R.string.log_saved, Toast.LENGTH_SHORT)
                startLogcatActivityToViewSavedFile(filename)
            } else {
                makeToast(R.string.unable_to_save_log, Toast.LENGTH_LONG)
            }
        }
    }

    private fun checkLogLine(line: String, searchCriteria: SearchCriteria, logLevelLimit: Int): Boolean {
        val logLine = LogLine.newLogLine(line, false)
        return searchCriteria.matches(logLine) &&
                LogLineAdapterUtil.logLevelIsAcceptableGivenLogLevelLimit(logLine.logLevel, logLevelLimit)
    }

    private fun startLogcatActivityToViewSavedFile(filename: String) {
        val targetIntent = Intent(applicationContext, LogcatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            action = Intent.ACTION_MAIN
            putExtra("filename", filename)
        }
        startActivity(targetIntent)
    }

    private fun makeToast(stringResId: Int, toastLength: Int) {
        handler.post { Toast.makeText(this@LogcatRecordingService, stringResId, toastLength).show() }
    }

    private fun killProcess() {
        if (!mKilled) {
            synchronized(lock) {
                if (!mKilled && mReader != null) {
                    mReader!!.killQuietly()
                    mKilled = true
                }
            }
        }
    }

    companion object {
        const val URI_SCHEME = "catlog_recording_service"
        const val EXTRA_FILENAME = "filename"
        const val EXTRA_LOADER = "loader"
        const val EXTRA_QUERY_FILTER = "filter"
        const val EXTRA_LEVEL = "level"
        private const val ACTION_STOP_RECORDING = "com.pluscubed.catlog.action.STOP_RECORDING"
        private val log = UtilLogger(LogcatRecordingService::class.java)
    }
}
