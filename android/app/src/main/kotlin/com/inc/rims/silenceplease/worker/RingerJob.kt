package com.inc.rims.silenceplease.worker

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest


class RingerJob: Job() {
    companion object {
        const val TAG = "RingerJob"
    }

    override fun onRunJob(params: Params): Result {
        Log.d(TAG, "At ringer")
        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_NORMAL
        return Result.SUCCESS
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG-$id").setExact(at)
                .build().schedule()
    }
}