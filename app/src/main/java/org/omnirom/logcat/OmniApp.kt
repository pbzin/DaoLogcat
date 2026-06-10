package org.omnirom.logcat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class DaoLogcatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder().setThemeOverlay(R.style.Theme_DaoLogcat_Overlay).build()
        )
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "com.pluscubed.logcat.notification"
    }
}
