package com.inc.rims.silenceplease.worker

import android.content.Context
import android.media.AudioManager
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import android.content.Intent
import android.app.NotificationManager
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.service.ForeService
import com.inc.rims.silenceplease.util.SharedPrefUtil


class RingerJob: Job() {
    companion object {
        const val TAG = "RingerJob"
    }

    override fun onRunJob(params: Params): Result {

        val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        service.ringerMode = AudioManager.RINGER_MODE_NORMAL

        SharedPrefUtil().clear(context, MainActivity.SHARED_PERF_CALL_SESSION_FILE)
        closeRunningForeService()
        return Result.SUCCESS
    }

    private fun closeRunningForeService() {
        val intent = Intent(ForeService.STOP_BROADCAST_ACTION)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun schedule(at: Long, id: String) {
        JobRequest.Builder("$TAG#$id").setExact(at)
                .build().schedule()
    }
}