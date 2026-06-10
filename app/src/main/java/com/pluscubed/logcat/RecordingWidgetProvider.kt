package com.pluscubed.logcat

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.pluscubed.logcat.helper.PreferenceHelper
import com.pluscubed.logcat.helper.WidgetHelper
import com.pluscubed.logcat.util.UtilLogger

class RecordingWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        log.i("onUpdate() for appWidgetIds %s", appWidgetIds.contentToString())
        PreferenceHelper.setWidgetExistsPreference(context, appWidgetIds)
        WidgetHelper.updateWidgets(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        log.i("onReceive(); intent is: %s", intent)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            PreferenceHelper.clearWidgetExistsPreference(context, id)
        }
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        for (i in oldWidgetIds.indices) {
            PreferenceHelper.remapWidgetExistsPreference(context, oldWidgetIds[i], newWidgetIds[i])
        }
    }

    companion object {
        const val ACTION_RECORD_OR_STOP = "com.pluscubed.logcat.action.RECORD_OR_STOP"
        val log = UtilLogger(RecordingWidgetProvider::class.java)
    }
}
