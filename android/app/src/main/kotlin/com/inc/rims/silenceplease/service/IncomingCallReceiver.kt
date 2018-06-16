package com.inc.rims.silenceplease.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.content.ContextCompat
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.inc.rims.silenceplease.MainActivity
import com.inc.rims.silenceplease.util.NotificationHelper
import com.inc.rims.silenceplease.util.SharedPrefUtil

class IncomingCallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState: Int = -1
    }

    private val ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var ringtone: Ringtone? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val bundle = intent.extras
            val telephonyManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val curState = telephonyManager.callState

            if (lastState != curState) {
                lastState = curState

                if (ringtone == null) {
                    ringtone = RingtoneManager.getRingtone(context, ringUri)
                }

                when (curState) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        val incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        if (getBoolPrefFromMain(context, MainActivity.SMS_SERVICE_ENABLE)) {
                            if (checkNumberAlreadyCall(context, incomingNumber!!)) {
                                val cur = getLongPref(context, incomingNumber)

                                if (cur == getLongPrefFromMain(context, MainActivity.SMS_SERVICE_ATTEMPTS)) {
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
                                // assumption that number start with 1 not with 0
                                editLongPref(context, incomingNumber, cur + 2)
                            } else {
                                editLongPref(context, incomingNumber, 1)
                            }
                        }
                        if (getBoolPrefFromMain(context, MainActivity.WHITE_LIST_SERVICE)) {
                            val result = SharedPrefUtil().all(context, MainActivity.SHARED_PERF_WHITE_LIST_FILE)
                            if (!result.isEmpty()) {
                                val phoneUtil = PhoneNumberUtil.getInstance()!!
                                var matched = false
                                for (x in result.values) {
                                    val match = phoneUtil.isNumberMatch((x as String), incomingNumber)
                                    if (match == PhoneNumberUtil.MatchType.NSN_MATCH ||
                                            match == PhoneNumberUtil.MatchType.EXACT_MATCH ||
                                            match == PhoneNumberUtil.MatchType.SHORT_NSN_MATCH) {
                                        matched = true
                                        break
                                    }
                                }
                                if (matched) {
                                    editBoolPrefFromMain(context, MainActivity.SILENCE_DISABLE_DUE_TO_MATCH, true)
                                    val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                                    editIntPrefFromMain(context, MainActivity.RINGER_STATE, service.ringerMode)
                                    service.ringerMode = AudioManager.RINGER_MODE_NORMAL
                                    if (ringtone != null && !ringtone!!.isPlaying) {
                                        ringtone?.play()
                                    }
                                }
                            }
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        if (getBoolPrefFromMain(context, MainActivity.SILENCE_DISABLE_DUE_TO_MATCH)) {
                            if (ringtone != null && ringtone!!.isPlaying) {
                                ringtone?.stop()
                            }
                            val service = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
                            service.ringerMode = getIntPrefFromMain(context, MainActivity.RINGER_STATE)
                            editBoolPrefFromMain(context, MainActivity.SILENCE_DISABLE_DUE_TO_MATCH,
                                    false)
                        }
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        if (ringtone != null && ringtone!!.isPlaying) {
                            ringtone?.stop()
                        }
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

    private fun editIntPrefFromMain(context: Context, key: String, value: Int) {
        SharedPrefUtil().editIntPref(context, MainActivity.SHARED_PERF_FILE, key,
                value)
    }

    private fun editBoolPrefFromMain(context: Context, key: String, value: Boolean) {
        SharedPrefUtil().editBoolPref(context, MainActivity.SHARED_PERF_FILE, key,
                value)
    }

    private fun getLongPrefFromMain(context: Context, key: String): Long {
        return SharedPrefUtil().getLongPref(context, MainActivity.SHARED_PERF_FILE, key,
                0L)
    }

    private fun getStringPrefFromMain(context: Context, key: String): String {
        return SharedPrefUtil().getStringPref(context, MainActivity.SHARED_PERF_FILE, key,
                "")
    }

    private fun getBoolPrefFromMain(context: Context, key: String): Boolean {
        return SharedPrefUtil().getBoolPref(context, MainActivity.SHARED_PERF_FILE, key,
                false)
    }

    private fun checkNumberAlreadyCall(context: Context, incomingNumber: String): Boolean {
        return context.getSharedPreferences(MainActivity.SHARED_PERF_CALL_SESSION_FILE, 0)
                .contains(incomingNumber)
    }
}