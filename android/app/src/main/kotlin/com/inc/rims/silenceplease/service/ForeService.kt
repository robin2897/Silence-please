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
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.util.ServiceUtil
import com.inc.rims.silenceplease.util.ServiceUtil.Companion.START_LISTEN
import com.inc.rims.silenceplease.util.ServiceUtil.Companion.STOP_SERVICE
import com.inc.rims.silenceplease.util.SharedPrefUtil

class ForeService: Service(){
    private val receiver = IncomingCallReceiver()
    private lateinit var localBroadcastReceiver: LocalBroadcastManager

    private val stopServiceBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == STOP_BROADCAST_ACTION) {
                ServiceUtil().stopForeService(this@ForeService, true)
            }
        }

    }

    private val stopServiceDueRingerBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                if (intent.extras.getInt(AudioManager.EXTRA_RINGER_MODE)
                        == AudioManager.RINGER_MODE_NORMAL) {
                    val stop = Intent(ForeService.STOP_BROADCAST_ACTION)
                    stop.addCategory(Intent.CATEGORY_DEFAULT)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(stop)
                }
            }
        }

    }

    companion object {
        const val STOP_BROADCAST_ACTION = "com.inc.rims.silenceplease.service/stopForeService"
    }

    override fun onCreate() {
        super.onCreate()
        localBroadcastReceiver = LocalBroadcastManager.getInstance(this)
        if (SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                        MainActivity.SMS_SERVICE_ENABLE, false)) {
            val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            this.registerReceiver(receiver, filter)
        }

        val ringerFilter = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
        this.registerReceiver(stopServiceDueRingerBroadcastReceiver, ringerFilter)

        val stopFilter = IntentFilter(STOP_BROADCAST_ACTION)
        stopFilter.addCategory(Intent.CATEGORY_DEFAULT)
        localBroadcastReceiver.registerReceiver(stopServiceBroadcastReceiver, stopFilter)
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
                        MainActivity.SMS_SERVICE_ENABLE, false)) {
            this.unregisterReceiver(receiver)
        }
        this.unregisterReceiver(stopServiceDueRingerBroadcastReceiver)
        localBroadcastReceiver.unregisterReceiver(stopServiceBroadcastReceiver)
        super.onDestroy()
    }
}