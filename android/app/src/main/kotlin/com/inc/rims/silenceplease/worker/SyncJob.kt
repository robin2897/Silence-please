package com.inc.rims.silenceplease.worker

import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.inc.rims.silenceplease.room.DataDatabase
import com.inc.rims.silenceplease.util.PerformWork
import com.inc.rims.silenceplease.util.Validation
import java.util.*

class SyncJob: Job() {
    companion object {
        const val TAG = "SyncJob"
    }

    override fun onRunJob(params: Params): Result {
        val db = DataDatabase.getInstance(context)!!
        val c = Calendar.getInstance()
        val todaySilences = db.getAllModelsAtParticularDay(c.get(Calendar.DAY_OF_WEEK))
        for (x in JobManager.instance().allJobRequests) {
            if (x.jobId == params.id) {
                continue
            }
            JobManager.instance().cancel(x.jobId)
        }
        todaySilences.forEach {model ->
            val isStartTimeAfterNow = Validation().checkIsTimeAfterNow(model.startTime)
            if (isStartTimeAfterNow) {
                if (model.isSilent) {
                    SilenceJob().schedule(getTimeDifference(model.startTime), model.id)
                } else {
                    VibrateJob().schedule(getTimeDifference(model.startTime), model.id)
                }
                RingerJob().schedule(getTimeDifference(model.endTime), model.id)
            }
        }
        return Result.SUCCESS
    }

    fun schedule() {
        JobRequest.Builder(TAG).startNow().build().schedule()
    }

    private fun getTimeDifference(milli: Long): Long {
        val nowAtEpochStart = Calendar.getInstance()
        nowAtEpochStart.set(Calendar.YEAR, 1970)
        nowAtEpochStart.set(Calendar.MONTH, Calendar.JANUARY)
        nowAtEpochStart.set(Calendar.DATE, 1)

        return milli - nowAtEpochStart.timeInMillis
    }
}