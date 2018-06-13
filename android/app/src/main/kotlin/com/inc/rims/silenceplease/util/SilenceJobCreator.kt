package com.inc.rims.silenceplease.util

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.inc.rims.silenceplease.worker.*

class SilenceJobCreator: JobCreator {
    override fun create(tag: String): Job? {
        return when {
            tag.contains(SilenceJob.TAG) -> SilenceJob()
            tag.contains(VibrateJob.TAG) -> VibrateJob()
            tag.contains(RingerJob.TAG) -> RingerJob()
            tag.contains(SyncJob.TAG) -> SyncJob()
            tag.contains(AddJob.TAG) -> AddJob()
            else -> null
        }
    }
}