package com.inc.rims.silenceplease.worker

import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class DailySyncJob: DailyJob() {
    companion object {
        const val TAG = "DailySyncJob"
    }

    override fun onRunDailyJob(params: Params): DailyJobResult {
        SyncJob().schedule()
        return DailyJobResult.SUCCESS
    }

    fun schedule() {
        DailyJob.schedule(JobRequest.Builder(TAG), TimeUnit.HOURS.toMillis(0),
                TimeUnit.MINUTES.toMillis(15))
    }
}