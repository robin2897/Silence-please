package com.inc.rims.silenceplease.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.inc.rims.silenceplease.R

@Suppress("PrivatePropertyName")
class NotificationHelper(base: Context): ContextWrapper(base) {
    private val CHANNEL_ID = "com.inc.rims.silenceplease#notification"
    private val CHANNEL_NAME = "General notifications"
    private var manager: NotificationManager? = null

    init {
       createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            getManager().createNotificationChannel(channel)
        }
    }

    private fun getManager(): NotificationManager {
        if (manager == null) {
            manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        }
        return manager as NotificationManager
    }

    fun getSilenceMessageChannel(title: String, body: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
    }

    fun getManagerCompat(): NotificationManagerCompat {
        return NotificationManagerCompat.from(this)
    }
}