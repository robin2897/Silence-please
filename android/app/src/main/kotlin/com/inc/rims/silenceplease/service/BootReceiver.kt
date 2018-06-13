package com.inc.rims.silenceplease.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.inc.rims.silenceplease.util.PerformWork

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            PerformWork().startDailySync(context!!)
        }
    }
}