package com.inc.rims.silenceplease.worker

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest

class VibrateJob: Job() {
    companion object {
        const val TAG = "VibrateJob"
    }

    override fun onRunJob(params: Params): Result {
        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        return Result.SUCCESS
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG-$id").setExact(at).build().schedule()
    }
}