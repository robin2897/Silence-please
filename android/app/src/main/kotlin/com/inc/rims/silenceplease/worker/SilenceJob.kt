package com.inc.rims.silenceplease.worker

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.inc.rims.silenceplease.util.NotificationHelper


class SilenceJob: Job() {
    companion object {
        const val TAG = "SilenceJob"
    }

    @SuppressLint("ApplySharedPref")
    override fun onRunJob(params: Params): Result {
        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_SILENT

        val helper = NotificationHelper(context)
        val mBuilder = helper.getSilenceMessageChannel("Silence please",
                "Your phone is now in silent mode.")
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