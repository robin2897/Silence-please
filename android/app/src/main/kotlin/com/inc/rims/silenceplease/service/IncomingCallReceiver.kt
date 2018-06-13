package com.inc.rims.silenceplease.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.util.NotificationHelper
import com.inc.rims.silenceplease.util.SharedPrefUtil

class IncomingCallReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val bundle = intent.extras
            val state = bundle.getString(TelephonyManager.EXTRA_STATE)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (checkNumberAlreadyCall(context!!, incomingNumber!!)) {
                        val cur = getLongPref(context, incomingNumber)

                        if (cur == getLongPrefFromMain(context, MainActivity.SMS_SERVICE_ATTEMPTS)){
                            val message = getStringPrefFromMain(context, MainActivity.SMS_SERVICE_MESSAGE)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val permissionSendSms = Manifest.permission.SEND_SMS
                                if (checkPermission(context, permissionSendSms)) {
                                    sendSms(incomingNumber, message)
                                } else {
                                    val helper = NotificationHelper(context)
                                    val nBuild = helper.getNormalNotification(
                                            "Silence please",
                                            "Unable to send sms. Permission not granted")
                                    val id = getIntPrefFromMain(context, MainActivity.NOTIFICATION_ID)
                                    helper.getManagerCompat().notify(id, nBuild.build())
                                }
                            } else {
                                sendSms(incomingNumber, message)
                            }
                        }
                        editLongPref(context, incomingNumber, cur + 1)
                    } else {
                        editLongPref(context, incomingNumber, 1)
                    }
                }
            }
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null,
                null)
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager
                .PERMISSION_GRANTED
    }

    private fun editLongPref(context: Context, key: String, value: Long) {
        SharedPrefUtil().editLongPref(context, MainActivity.SHARED_PERF_CALL_SESSION_FILE, key,
                value)
    }

    private fun getLongPref(context: Context, key: String): Long {
        return SharedPrefUtil().getLongPref(context, MainActivity.SHARED_PERF_CALL_SESSION_FILE, key,
                0L)
    }

    private fun getIntPrefFromMain(context: Context, key: String): Int {
        return SharedPrefUtil().getIntPref(context, MainActivity.SHARED_PERF_FILE, key,
                0)
    }

    private fun getLongPrefFromMain(context: Context, key: String): Long {
        return SharedPrefUtil().getLongPref(context, MainActivity.SHARED_PERF_FILE, key,
                0L)
    }

    private fun getStringPrefFromMain(context: Context, key: String): String {
        return SharedPrefUtil().getStringPref(context, MainActivity.SHARED_PERF_FILE, key,
                "")
    }

    fun checkNumberAlreadyCall(context: Context, incomingNumber: String): Boolean {
        return context.getSharedPreferences(MainActivity.SHARED_PERF_CALL_SESSION_FILE, 0)
                .contains(incomingNumber)
    }
}