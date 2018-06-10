package com.inc.rims.silenceplease.worker

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.inc.rims.silenceplease.util.NotificationHelper
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.content.Intent
import android.app.NotificationManager
import android.os.Build


class RingerJob: Job() {
    companion object {
        const val TAG = "RingerJob"
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
                        Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            }
        }

        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_NORMAL

        val helper = NotificationHelper(context)
        val mBuilder = helper.getSilenceMessageChannel("Silence please",
                "Your phone is now in ringer mode.")
        val notificationManager = helper.getManagerCompat()

        val id = context.getSharedPreferences("FlutterSharedPreferences", 0)
                .getInt("notifyId", 1)
        notificationManager.notify(id, mBuilder.build())
        context.getSharedPreferences("FlutterSharedPreferences", 0).edit()
                .putInt("notifyId", id + 1).commit()

        return Result.SUCCESS
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG-$id").setExact(at)
                .build().schedule()
    }
}