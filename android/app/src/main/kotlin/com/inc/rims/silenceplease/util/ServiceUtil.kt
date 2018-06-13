package com.inc.rims.silenceplease.util

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.JobManager
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.R
import com.inc.rims.silenceplease.service.ForeService
import com.inc.rims.silenceplease.worker.RingerJob

class ServiceUtil {

    companion object {
        const val STOP_SERVICE = "stop"
        const val START_LISTEN = "listen"
    }

    fun startForeService(title: String, body: String, id: Int, context: Context, ref: String = "",
                         isAction: Boolean, serviceClass: Class<*>) {
        val startForeService = Intent(context, serviceClass)
        startForeService.action = START_LISTEN
        startForeService.putExtra("title", title)
        startForeService.putExtra("body", body)
        startForeService.putExtra("id", id)

        if (isAction) {
            SharedPrefUtil().editStringPref(context, MainActivity.SHARED_PERF_FILE,
                    MainActivity.NOTIFICATION_ACTIVE_MODEL_UUID, ref)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startForeService)
        } else {
            context.startService(startForeService)
        }
    }

    fun getServiceIndent(title: String, body: String, id: Int, context: Context,
                         serviceClass: Class<*>): Intent {
        val startForeService = Intent(context, serviceClass)
        startForeService.action = START_LISTEN
        startForeService.putExtra("title", title)
        startForeService.putExtra("body", body)
        startForeService.putExtra("id", id)
        SharedPrefUtil().editIntPref(context, MainActivity.SHARED_PERF_FILE,
                MainActivity.NOTIFICATION_SYNC_ID, id)
        return startForeService
    }

    fun stopForeService(service: Service, isAction: Boolean) {
        if (isAction) {
            val ref = SharedPrefUtil().getStringPref(service, MainActivity.SHARED_PERF_FILE,
                    MainActivity.NOTIFICATION_ACTIVE_MODEL_UUID, "")
            JobManager.instance().cancelAllForTag("${RingerJob.TAG}#$ref")
            SharedPrefUtil().clear(service, MainActivity.SHARED_PERF_CALL_SESSION_FILE)
            val audio = (service.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }
        service.stopForeground(true)
        service.stopSelf()
    }

    fun showForegroundNotification(intent: Intent, service: Service, isAction: Boolean,
                                   action: PendingIntent? = null) {
        val helper = NotificationHelper(service)
        val notification = if (isAction) {
            helper.getActionNotification(
                    intent.getStringExtra("title"),
                    intent.getStringExtra("body"), action!!, R.drawable.ic_close_black_24dp,
                    "Cancel")
        } else {
            helper.getNormalNotification(intent.getStringExtra("title"),
                    intent.getStringExtra("body"))
        }
        service.startForeground(intent.getIntExtra("id", -1),
                notification.build())
    }
}