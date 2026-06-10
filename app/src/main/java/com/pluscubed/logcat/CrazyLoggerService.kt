package com.pluscubed.logcat

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import com.pluscubed.logcat.util.UtilLogger
import java.util.*

class CrazyLoggerService : IntentService("CrazyLoggerService") {
    private var kill = false

    override fun onHandleIntent(intent: Intent?) {
        log.d("onHandleIntent()")
        while (!kill) {
            try {
                Thread.sleep(INTERVAL)
            } catch (e: InterruptedException) {
                log.e(e, "error")
            }
            val date = Date()
            log.i("Log message " + date + " " + date.time % 1000)
            if (Random().nextInt(100) % 5 == 0) {
                log.i("email: emailme@hello.com")
                log.i("ftp: ftp://website.com:21/")
                log.i("http: https://website.com/")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        kill = true
    }

    companion object {
        private const val INTERVAL: Long = 300
        private val log = UtilLogger(CrazyLoggerService::class.java)
    }
}
