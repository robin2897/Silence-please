package com.inc.rims.silenceplease.util

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import com.inc.rims.silenceplease.MainActivity

class Util {

    fun alarmPendingIndent(context: Context, intent: Intent): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT)
        }
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.
                PERMISSION_GRANTED
    }

    fun firstLanding(context: Context) {
        val isFirst = SharedPrefUtil.getBoolPref(context, MainActivity.SHARED_PERF_FILE,
                MainActivity.IS_FIRST_RUN, true)
        if(isFirst) {
            SharedPrefUtil.editIntPref(context, MainActivity.SHARED_PERF_FILE,
                    MainActivity.NOTIFICATION_ID, 1)
            SharedPrefUtil.editBoolPref(context, MainActivity.SHARED_PERF_FILE,
                    MainActivity.IS_FIRST_RUN, false)
        }
    }
}