package com.inc.rims.silenceplease.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.R
import com.inc.rims.silenceplease.service.ForeService
import com.inc.rims.silenceplease.util.NotificationHelper
import com.inc.rims.silenceplease.util.ServiceUtil
import com.inc.rims.silenceplease.util.SharedPrefUtil

class VibrateJob: Job() {
    companion object {
        const val TAG = "VibrateJob"
    }

    override fun onRunJob(params: Params): Result {
        var run = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            val n = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (n.isNotificationPolicyAccessGranted) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                run = false
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                val helper = NotificationHelper(context)
                val nBuild = helper.getActionNotification("Silence please",
                        "You have not granted Do not disturb permission",pendingIntent,
                        R.drawable.ic_settings_black_24dp, "Settings")
                val id = getNotificationId()
                helper.getManagerCompat().notify(id, nBuild.build())
                updatePref(MainActivity.NOTIFICATION_ID, id + 1)
                closeRunningForeService()
            }
        }

        if (run) {
            val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            service.ringerMode = AudioManager.RINGER_MODE_VIBRATE

            val id = getNotificationId()
            val ref = params.tag.split("#")[1]

            startForeService(id, ref)

            updatePref(MainActivity.NOTIFICATION_ID, id + 1)
        }
        return Result.SUCCESS
    }

    private fun closeRunningForeService() {
        val intent = Intent(ForeService.STOP_SERVICE_ACTION)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        context.sendBroadcast(intent)
    }

    private fun startForeService(id: Int, ref: String) {
        ServiceUtil().startForeService("Silence please",
                "Your phone is now in vibrate mode", id, context, ref, true,
                ForeService::class.java)
    }

    private fun getNotificationId() = SharedPrefUtil().getIntPref(context,
            MainActivity.SHARED_PERF_FILE, MainActivity.NOTIFICATION_ID, 1)

    private fun updatePref(key: String, value: Int) {
        SharedPrefUtil().editIntPref(context, MainActivity.SHARED_PERF_FILE, key, value)
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG#$id").setExact(at).build().schedule()
    }
}