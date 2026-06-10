package com.pluscubed.logcat.helper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pluscubed.logcat.LogcatRecordingService
import com.pluscubed.logcat.RecordingWidgetProvider
import com.pluscubed.logcat.ui.RecordLogDialogActivity
import com.pluscubed.logcat.util.UtilLogger
import org.omnirom.logcat.R

object WidgetHelper {
    private val log = UtilLogger(WidgetHelper::class.java)

    @JvmStatic
    fun updateWidgets(context: Context) {
        val appWidgetIds = findAppWidgetIds(context)
        updateWidgets(context, appWidgetIds)
    }

    @JvmStatic
    fun updateWidgets(context: Context, serviceRunning: Boolean) {
        val appWidgetIds = findAppWidgetIds(context)
        updateWidgets(context, appWidgetIds, serviceRunning)
    }

    @JvmStatic
    fun updateWidgets(context: Context, appWidgetIds: IntArray) {
        val serviceRunning = ServiceHelper.checkIfServiceIsRunning(context, LogcatRecordingService::class.java)
        updateWidgets(context, appWidgetIds, serviceRunning)
    }

    @JvmStatic
    fun updateWidgets(context: Context, appWidgetIds: IntArray, serviceRunning: Boolean) {
        val manager = AppWidgetManager.getInstance(context)
        for (appWidgetId in appWidgetIds) {
            if (!PreferenceHelper.getWidgetExistsPreference(context, appWidgetId)) {
                log.d("Found stale app widget id %d; skipping...", appWidgetId)
                continue
            }
            updateWidget(context, manager, appWidgetId, serviceRunning)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int, serviceRunning: Boolean) {
        val updateViews = RemoteViews(context.packageName, R.layout.widget_recording)
        updateViews.setImageViewResource(
            R.id.record_badge_image_view,
            if (serviceRunning) R.drawable.ic_widget_stop_record else R.drawable.ic_widget_start_record
        )
        val pendingIntent = getPendingIntent(context, appWidgetId)
        updateViews.setOnClickPendingIntent(R.id.record_badge_view, pendingIntent)
        manager.updateAppWidget(appWidgetId, updateViews)
    }

    private fun getPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent().apply {
            setClass(context, RecordLogDialogActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            action = RecordingWidgetProvider.ACTION_RECORD_OR_STOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun findAppWidgetIds(context: Context): IntArray {
        val manager = AppWidgetManager.getInstance(context)
        val widget = ComponentName(context, RecordingWidgetProvider::class.java)
        return manager.getAppWidgetIds(widget)
    }
}
