package com.inc.rims.silenceplease.worker

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.inc.rims.silenceplease.util.NotificationHelper

class VibrateJob: Job() {
    companion object {
        const val TAG = "VibrateJob"
    }

    @SuppressLint("ApplySharedPref")
    override fun onRunJob(params: Params): Result {
        val n = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            if (n.isNotificationPolicyAccessGranted) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            } else {
                val intent =
                        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        }

        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_VIBRATE

        val helper = NotificationHelper(context)
        val mBuilder = helper.getSilenceMessageChannel("Silence please",
                "Your phone is now in vibrate mode.")
        val notificationManager = helper.getManagerCompat()

        val id = context.getSharedPreferences("FlutterSharedPreferences", 0)
                .getInt("notifyId", 1)
        notificationManager.notify(id, mBuilder.build())
        context.getSharedPreferences("FlutterSharedPreferences", 0).edit()
                .putInt("notifyId", id + 1).commit()

        return Result.SUCCESS
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG-$id").setExact(at).build().schedule()
    }
}