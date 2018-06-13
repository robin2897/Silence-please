package com.inc.rims.silenceplease.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.worker.AddJob
import com.inc.rims.silenceplease.worker.SyncJob
import java.util.*


class PerformWork {

    fun startDailySync(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calender = Calendar.getInstance()
        calender.set(Calendar.HOUR_OF_DAY, 0)
        calender.set(Calendar.MINUTE, 0)
        calender.add(Calendar.DAY_OF_MONTH, 1)
        val intent = ServiceUtil().getServiceIndent("Silence", "Syncing all silence",
                getNotificationId(context), context, PerformSyncJob::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calender.timeInMillis,
                    pendingIntent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, calender.timeInMillis, pendingIntent)
            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, calender.timeInMillis, pendingIntent)
            }
        }
    }

    fun performJobAdd(j: String) {
        AddJob().schedule(j)
    }

    fun performSync() {
        SyncJob().schedule()
    }

    private fun getNotificationId(context: Context) = SharedPrefUtil().getIntPref(context,
            MainActivity.SHARED_PERF_FILE, MainActivity.NOTIFICATION_ID, 1)

    class PerformSyncJob: Service() {

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent!!.action) {
                ServiceUtil.START_LISTEN -> {
                    ServiceUtil().showForegroundNotification(intent, this, false)
                    SyncJob().schedule()
                    PerformWork().startDailySync(this)
                    ServiceUtil().stopForeService(this@PerformSyncJob, false)
                }
            }
            return super.onStartCommand(intent, flags, startId)
        }
    }
}