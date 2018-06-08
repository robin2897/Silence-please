package com.inc.rims.silenceplease.util

import com.inc.rims.silenceplease.worker.AddJob
import com.inc.rims.silenceplease.worker.DailySyncJob
import com.inc.rims.silenceplease.worker.SyncJob


class PerformWork {

    fun startDailySync() {
        DailySyncJob().schedule()
    }

    fun performJobAdd(j: String) {
        AddJob().schedule(j)
    }

    fun performSync() {
        SyncJob().schedule()
    }
}