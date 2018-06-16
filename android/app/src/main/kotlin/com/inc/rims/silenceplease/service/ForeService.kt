package com.inc.rims.silenceplease.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.telephony.TelephonyManager
import android.util.Log
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.util.ServiceUtil
import com.inc.rims.silenceplease.util.ServiceUtil.Companion.START_LISTEN
import com.inc.rims.silenceplease.util.ServiceUtil.Companion.STOP_SERVICE
import com.inc.rims.silenceplease.util.SharedPrefUtil

class ForeService : Service() {
    private var receiver: BroadcastReceiver? = IncomingCallReceiver()
    private var localBroadcastReceiver: LocalBroadcastManager? = null

    private var stopServiceBroadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == STOP_SERVICE_ACTION) {
                ServiceUtil().stopForeService(this@ForeService, true)
            }
        }

    }

    private var stopServiceDueRingerBroadcastReceiver: BroadcastReceiver? =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent!!.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                        if (intent.extras.getInt(AudioManager.EXTRA_RINGER_MODE)
                                == AudioManager.RINGER_MODE_NORMAL) {
                            if (!SharedPrefUtil().getBoolPref(context!!, MainActivity.SHARED_PERF_FILE,
                                            MainActivity.SILENCE_DISABLE_DUE_TO_MATCH, false)){
                                val stop = Intent(ForeService.STOP_SERVICE_ACTION)
                                stop.addCategory(Intent.CATEGORY_DEFAULT)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(stop)
                            }
                        }
                    }
                }
            }

    companion object {
        const val STOP_SERVICE_ACTION = "com.inc.rims.silenceplease.service/stopForeService"
        const val SERVICE_STARTED_ACTION = "com.inc.rims.silenceplease.service/startForeService"
    }

    override fun onCreate() {
        super.onCreate()
        localBroadcastReceiver = LocalBroadcastManager.getInstance(this)

        if (SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                        MainActivity.SMS_SERVICE_ENABLE, false) ||
                SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                        MainActivity.WHITE_LIST_SERVICE, false)) {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            this.registerReceiver(receiver, filter)
        }

        val ringerFilter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        this.registerReceiver(stopServiceDueRingerBroadcastReceiver, ringerFilter)

        val stopFilter = IntentFilter(STOP_SERVICE_ACTION)
        stopFilter.addCategory(Intent.CATEGORY_DEFAULT)
        localBroadcastReceiver?.registerReceiver(stopServiceBroadcastReceiver, stopFilter)

        localBroadcastReceiver?.sendBroadcast(Intent(SERVICE_STARTED_ACTION)
                .addCategory(Intent.CATEGORY_DEFAULT))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                STOP_SERVICE -> {
                    ServiceUtil().stopForeService(this, true)
                }
                START_LISTEN -> {
                    val stopForeService = Intent(this, ForeService::class.java)
                    stopForeService.action = STOP_SERVICE
                    val stopIntent = PendingIntent.getService(this, 0,
                            stopForeService, 0)
                    ServiceUtil().showForegroundNotification(intent, this, true,
                            stopIntent)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                        MainActivity.SMS_SERVICE_ENABLE, false)||
                SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                        MainActivity.WHITE_LIST_SERVICE, false)) {
            this.unregisterReceiver(receiver)
        }
        this.unregisterReceiver(stopServiceDueRingerBroadcastReceiver)
        localBroadcastReceiver?.unregisterReceiver(stopServiceBroadcastReceiver)
        stopServiceBroadcastReceiver = null
        stopServiceDueRingerBroadcastReceiver = null
        receiver = null
        localBroadcastReceiver = null
        super.onDestroy()
    }
}