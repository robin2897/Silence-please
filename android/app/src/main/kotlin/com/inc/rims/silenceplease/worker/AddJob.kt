package com.inc.rims.silenceplease.worker

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.google.gson.Gson
import com.inc.rims.silenceplease.room.DataModel
import com.inc.rims.silenceplease.util.Validation
import java.util.*

class AddJob: Job() {
    companion object {
        const val TAG = "AddJob"
    }

    override fun onRunJob(params: Params): Result {
        val gson = Gson()
        val modelJson = params.extras["model_json"] as String
        val model = gson.fromJson<DataModel>(modelJson, DataModel::class.java)

        val isStartTimeAfterNow = Validation().checkIsTimeAfterNow(model.startTime)
        val isEndTimeAfterNow = Validation().checkIsTimeAfterNow(model.endTime)
        if (isStartTimeAfterNow) {
            if (model.isSilent) {
                SilenceJob().schedule(getTimeDifference(model.startTime), model.id)
            } else {
                VibrateJob().schedule(getTimeDifference(model.startTime), model.id)
            }
        }
        if (isEndTimeAfterNow) {
            RingerJob().schedule(getTimeDifference(model.endTime), model.id)
        }
        return Result.SUCCESS
    }

    fun schedule(jsonModel: String) {
        val extras = PersistableBundleCompat()
        extras.putString("model_json", jsonModel)
        JobRequest.Builder(TAG).setExtras(extras).startNow().build().schedule()
    }

    private fun getTimeDifference(milli: Long): Long {
        val nowAtEpochStart = Calendar.getInstance()
        nowAtEpochStart.set(Calendar.YEAR, 1970)
        nowAtEpochStart.set(Calendar.MONTH, Calendar.JANUARY)
        nowAtEpochStart.set(Calendar.DATE, 1)

        return milli - nowAtEpochStart.timeInMillis
    }
}