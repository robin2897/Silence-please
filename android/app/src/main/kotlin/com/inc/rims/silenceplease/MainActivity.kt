package com.inc.rims.silenceplease

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.persistence.db.SupportSQLiteQueryBuilder
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.evernote.android.job.JobManager
import com.google.gson.Gson
import com.inc.rims.silenceplease.room.DataDatabase
import com.inc.rims.silenceplease.room.DataModel
import com.inc.rims.silenceplease.room.JsonArrayDataModel
import com.inc.rims.silenceplease.service.BootReceiver
import com.inc.rims.silenceplease.util.Validation
import com.inc.rims.silenceplease.util.PerformWork
import com.inc.rims.silenceplease.util.ServiceUtil
import com.inc.rims.silenceplease.util.SharedPrefUtil
import com.inc.rims.silenceplease.worker.RingerJob
import com.inc.rims.silenceplease.worker.SilenceJob
import com.inc.rims.silenceplease.worker.VibrateJob
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList


class MainActivity : FlutterActivity() {

    private val channel = "com.inc.rims.silenceplease/database"
    private val tag = MainActivity::class.simpleName
    private val disposable = mutableMapOf<String, Disposable>()

    @Inject
    lateinit var gson: Gson

    companion object {
        private const val PERMISSION_SMS_SERVICE = 1

        const val SHARED_PERF_FILE = "FlutterSharedPreferences"
        const val SHARED_PERF_CALL_SESSION_FILE = "CallSession"

        const val NOTIFICATION_ID = "flutter.notifyId"
        const val NOTIFICATION_SYNC_ID = "flutter.notifySyncId"
        const val NOTIFICATION_ACTIVE_MODEL_UUID = "flutter.activeJobId"
        const val FIRST_INSERT = "flutter.isFirstInsert"
        const val IS_FIRST_RUN = "flutter.isFirst"

        const val SMS_SERVICE_ENABLE = "flutter.SMS_SERVICE"
        const val SMS_SERVICE_MESSAGE = "flutter.SMS_SERVICE_MESSAGE"
        const val SMS_SERVICE_ATTEMPTS = "flutter.SMS_SERVICE_ATTEMPTS"
        const val SILENCE_IS_ENABLE = "flutter.IS_ENABLE"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
        (application as BaseApp).getAppComponent().inject(this)
        val db = DataDatabase.getInstance(this)!!
        firstLanding()
        shouldStartBootReceiver()

        MethodChannel(flutterView, channel).setMethodCallHandler { call, result ->
            val key = call.method
            when (call.method) {
                "insertDB" -> {
                    val json = call.argument<String>("model")
                    val model = gson.fromJson<DataModel>(json, DataModel::class.java)

                    Completable.fromAction {
                        if (!db.isOpen) db.openHelper.writableDatabase
                        db.dataModelDao().insert(model)

                        if (Validation().checkTodayDayMatch(model.days)) {
                            PerformWork().performJobAdd(json)
                        }

                        val isFirstInsert = SharedPrefUtil().getBoolPref(this,
                                SHARED_PERF_FILE, "isFirstInsert", true)
                        if (isFirstInsert) {
                            PerformWork().startDailySync(this)
                            val componentName = ComponentName(this, BootReceiver::class.java)
                            packageManager.setComponentEnabledSetting(componentName,
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP)
                            SharedPrefUtil().editBoolPref(this, SHARED_PERF_FILE,
                                    FIRST_INSERT, false)
                        }
                    }.subscribeOn(Schedulers.io())
                            .subscribe(object : CompletableObserver {
                                override fun onComplete() {
                                    result.success(0)
                                }

                                override fun onSubscribe(d: Disposable) {
                                    if (disposable.containsKey(key)) {
                                        disposable[key]?.dispose()
                                        disposable[key] = d
                                    } else {
                                        disposable[key] = d
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    result.error(tag, key, e.message)
                                    Log.e(tag, key, e)
                                    disposable[key]?.dispose()
                                }
                            })
                }
                "updateDB" -> {
                    val json = call.argument<String>("model")
                    val model = gson.fromJson<DataModel>(json, DataModel::class.java)
                    Completable.fromAction {
                        if (!db.isOpen) db.openHelper.writableDatabase
                        Log.d("UPDATE ID", model.id)
                        val old = db.dataModelDao().getSingle(model.id).blockingGet()
                        db.dataModelDao().update(model)

                        if (old.isSilent){
                            JobManager.instance()
                                    .cancelAllForTag("${SilenceJob.TAG}#${model.id}")
                        } else {
                            JobManager.instance()
                                    .cancelAllForTag("${VibrateJob.TAG}#${model.id}")
                        }
                        JobManager.instance().cancelAllForTag("${RingerJob.TAG}#${model.id}")
                        if (model.isActive) {
                            if (Validation().checkTodayDayMatch(model.days)) {
                                PerformWork().performJobAdd(json)
                            }
                        }
                    }.subscribeOn(Schedulers.io()).subscribe(
                            object : CompletableObserver {
                                override fun onComplete() {
                                    result.success(0)
                                }

                                override fun onSubscribe(d: Disposable) {
                                    if (disposable.containsKey(key)) {
                                        disposable[key]?.dispose()
                                        disposable[key] = d
                                    } else {
                                        disposable[key] = d
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    result.error(tag, key, e.message)
                                    Log.e(tag, key, e)
                                    disposable[key]?.dispose()
                                }

                            }
                    )
                }
                "deleteDB" -> {
                    val json = call.argument<String>("model")
                    val model = gson.fromJson<DataModel>(json, DataModel::class.java)
                    Completable.fromAction {
                        if (!db.isOpen) db.openHelper.writableDatabase
                        db.dataModelDao().delete(model)

                        if (Validation().checkTodayDayMatch(model.days)) {
                            if (model.isSilent) {
                                JobManager.instance().
                                        cancelAllForTag("${SilenceJob.TAG}#${model.id}")
                            } else {
                                JobManager.instance().
                                        cancelAllForTag("${VibrateJob.TAG}#${model.id}")
                            }
                        }
                        JobManager.instance().cancelAllForTag("${RingerJob.TAG}#${model.id}")
                    }.subscribeOn(Schedulers.io()).subscribe(
                            object : CompletableObserver {
                                override fun onComplete() {
                                    result.success(0)
                                }

                                override fun onSubscribe(d: Disposable) {
                                    if (disposable.containsKey(key)) {
                                        disposable[key]?.dispose()
                                        disposable[key] = d
                                    } else {
                                        disposable[key] = d
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    result.error(tag, key, e.message)
                                    Log.e(tag, key, e)
                                    disposable[key]?.dispose()
                                }

                            }
                    )
                }
                "getAllDB" -> {
                    db.dataModelDao().getAll()
                            .subscribeOn(Schedulers.io())
                            .subscribe(object : SingleObserver<List<DataModel>> {
                                override fun onSuccess(t: List<DataModel>) {
                                    val model = gson.toJson(JsonArrayDataModel(t),
                                            JsonArrayDataModel::class.java)
                                    result.success(model)
                                }

                                override fun onSubscribe(d: Disposable) {
                                    if (disposable.containsKey(key)) {
                                        disposable[key]?.dispose()
                                        disposable[key] = d
                                    } else {
                                        disposable[key] = d
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    result.error(tag, key, e.message)
                                    Log.e(tag, key, e)
                                    disposable[key]?.dispose()
                                }

                            })
                }
                "getNextDB" -> {
                    val calendar = Calendar.getInstance()
                    val now = Calendar.getInstance()
                    now.timeInMillis = System.currentTimeMillis()
                    calendar.set(1970, Calendar.JANUARY, 1, now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE))
                    val calMillisecond = calendar.timeInMillis

                    disposable[key] = db.dataModelDao().getNextSilence(SupportSQLiteQueryBuilder
                            .builder("timetable")
                            .selection("startTime > $calMillisecond", null)
                            .orderBy("startTime")
                            .limit("1")
                            .create()
                    ).subscribeOn(Schedulers.io())
                            .doOnError {
                                disposable["Inner$key"] =
                                        db.dataModelDao().getNextSilence(SupportSQLiteQueryBuilder
                                                .builder("timetable")
                                                .orderBy("startTime")
                                                .limit("1")
                                                .create()
                                        ).subscribe({ model, error ->
                                            if (error == null) {
                                                val json = gson.toJson(model, DataModel::class.java)
                                                result.success(json)
                                            }
                                        })
                            }.subscribe({ model, error ->
                                if (error == null) {
                                    val json = gson.toJson(model, DataModel::class.java)
                                    result.success(json)
                                }
                            })

                }
                "enableToggle" -> {
                    Completable.timer(1000L, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                val isEnabled = SharedPrefUtil().getBoolPref(this,
                                        SHARED_PERF_FILE, SILENCE_IS_ENABLE, true)
                                if (!isEnabled) {
                                    JobManager.instance().cancelAll()
                                    val componentName = ComponentName(this, BootReceiver::class.java)
                                    packageManager.setComponentEnabledSetting(componentName,
                                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                            PackageManager.DONT_KILL_APP)
                                    val id = SharedPrefUtil().getIntPref(this,
                                            SHARED_PERF_FILE, NOTIFICATION_SYNC_ID, 0)
                                    val intent = ServiceUtil().getServiceIndent("Silence",
                                            "Syncing all silence",
                                            id, this,PerformWork.PerformSyncJob::class.java)
                                    val alarm = getSystemService(Context.ALARM_SERVICE)
                                            as AlarmManager
                                    alarm.cancel(alarmPendingIndent(intent))
                                } else {
                                    val models = db.dataModelDao().getAll().blockingGet()
                                    if (models?.size != 0) {
                                        PerformWork().performSync()
                                        PerformWork().startDailySync(this)
                                    }
                                }
                            }
                }
                "checkPermission" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                        val whichPermission = call.argument<String>("permission")
                        when (whichPermission) {
                            "notification-policy" -> {
                                val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                                        NotificationManager
                                if (!nManager.isNotificationPolicyAccessGranted) {
                                    result.success("denied")
                                    val intent = Intent(
                                            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    startActivity(intent)
                                } else {
                                    result.success("granted")
                                }
                            }
                            "sms-service" -> {
                                val permissionPhoneState = Manifest.permission.READ_PHONE_STATE
                                var phoneStateGranted = false
                                val permissionSendSms = Manifest.permission.SEND_SMS
                                var sendSmsGranted = false

                                if (isPermissionGranted(permissionPhoneState)) {
                                    phoneStateGranted = true
                                }
                                if (isPermissionGranted(permissionSendSms)) {
                                    sendSmsGranted = true
                                }

                                if (!phoneStateGranted || !sendSmsGranted) {
                                    val permissions: ArrayList<String> = arrayListOf()
                                    if (!phoneStateGranted)
                                        permissions.add(Manifest.permission.READ_PHONE_STATE)
                                    if (!sendSmsGranted)
                                        permissions.add(Manifest.permission.SEND_SMS)
                                    ActivityCompat.requestPermissions(this,
                                            permissions.toTypedArray(), PERMISSION_SMS_SERVICE)
                                }

                                if (!isPermissionGranted(permissionPhoneState)
                                        || !isPermissionGranted(permissionSendSms)) {
                                    var value = "rationale."
                                    if(!shouldShowRequestPermissionRationale(permissionPhoneState)){
                                        value += "1"
                                    }

                                    if(!shouldShowRequestPermissionRationale(permissionSendSms)) {
                                        value += "2"
                                    }
                                    result.success(value)
                                }
                                result.success("granted")
                            }

                        }
                    }
                    else {
                        result.success("granted")
                    }
                }
                "redirectPermissionSetting" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 0)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onDestroy() {
        shouldStartBootReceiver()
        for (v in disposable.values) {
            if (!v.isDisposed)
                v.dispose()
        }
        super.onDestroy()
    }

    private fun alarmPendingIndent(intent: Intent): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(this, 0, intent, PendingIntent.
                    FLAG_UPDATE_CURRENT)
        }
    }

    private fun shouldStartBootReceiver() {
        if (disposable.contains("shouldStartBoot")) {
            disposable["shouldStartBoot"]?.dispose()
        }
        val db = DataDatabase.getInstance(this)!!
        val calender = Calendar.getInstance()
        calender.add(Calendar.DAY_OF_MONTH, 1)
        disposable["shouldStartBoot"] = Completable.fromAction {
            val list = db.getAllModelsAtParticularDay(calender[Calendar.DAY_OF_WEEK])
            if (list.isEmpty()) {
                val componentName = ComponentName(this, BootReceiver::class.java)
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP)
            } else {
                val componentName = ComponentName(this, BootReceiver::class.java)
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP)
            }
        }.subscribeOn(Schedulers.io()).subscribe({
            disposable["shouldStartBoot"]?.dispose()
        })
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.
                PERMISSION_GRANTED
    }

    private fun firstLanding() {
        val isFirst = SharedPrefUtil().getBoolPref(this, MainActivity.SHARED_PERF_FILE,
                IS_FIRST_RUN, true)
        if(isFirst) {
            SharedPrefUtil().editIntPref(this, MainActivity.SHARED_PERF_FILE,
                    MainActivity.NOTIFICATION_ID, 1)
            SharedPrefUtil().editBoolPref(this, MainActivity.SHARED_PERF_FILE,
                    MainActivity.IS_FIRST_RUN, false)
        }
    }
}
