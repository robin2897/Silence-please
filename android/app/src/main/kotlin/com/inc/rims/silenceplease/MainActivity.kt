package com.inc.rims.silenceplease

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.arch.persistence.db.SupportSQLiteQueryBuilder
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.util.Patterns
import com.evernote.android.job.JobManager
import com.inc.rims.silenceplease.room.*
import com.inc.rims.silenceplease.service.BootReceiver
import com.inc.rims.silenceplease.service.ForeService
import com.inc.rims.silenceplease.util.*
import com.inc.rims.silenceplease.worker.RingerJob
import com.inc.rims.silenceplease.worker.SilenceJob
import com.inc.rims.silenceplease.worker.VibrateJob
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AbstractActivity() {
    private val tag = "MainActivity"
    private lateinit var permissionCallback: PermissionCallback

    companion object {
        private const val PERMISSION_SMS_SERVICE = 1
        private const val PERMISSION_CONTACT_SERVICE = 2

        const val SHARED_PERF_FILE = "FlutterSharedPreferences"
        const val SHARED_PERF_CALL_SESSION_FILE = "CallSession"
        const val SHARED_PERF_WHITE_LIST_FILE = "WhiteList"
        const val SHARED_PERF_PERMISSION_FILE = "permission"

        const val NOTIFICATION_ID = "flutter.notifyId"
        const val NOTIFICATION_SYNC_ID = "flutter.notifySyncId"
        const val NOTIFICATION_ACTIVE_MODEL_UUID = "flutter.activeJobId"
        const val FIRST_INSERT = "flutter.isFirstInsert"
        const val IS_FIRST_RUN = "flutter.isFirst"

        const val SMS_SERVICE_ENABLE = "flutter.SMS_SERVICE"
        const val SMS_SERVICE_MESSAGE = "flutter.SMS_SERVICE_MESSAGE"
        const val SMS_SERVICE_ATTEMPTS = "flutter.SMS_SERVICE_ATTEMPTS"
        const val WHITE_LIST_SERVICE = "flutter.WHITE_LIST_SERVICE"
        const val RINGER_STATE = "flutter.RINGER_STATE"
        const val SILENCE_IS_ENABLE = "flutter.IS_ENABLE"
        const val IS_SILENCE_ACTIVE = "flutter.IS_SILENCE_ACTIVE"
        const val SILENCE_DISABLE_DUE_TO_MATCH = "flutter.SILENCE_DISABLE_DUE_TO_MATCH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
        (application as BaseApp).getAppComponent().inject(this)
        val db = DataDatabase.getInstance(this)!!
        Util().firstLanding(this)
        shouldStartBootReceiver()
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)

        MethodChannel(flutterView, channelMethod).setMethodCallHandler { call, result ->
            permissionCallback = object : PermissionCallback {
                override fun sendResult(send: String) {
                    result.success(send)
                }
            }

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

                        val isFirstInsert = SharedPrefUtil.getBoolPref(this,
                                SHARED_PERF_FILE, "isFirstInsert", true)
                        if (isFirstInsert) {
                            PerformWork().startDailySync(this)
                            val componentName = ComponentName(this, BootReceiver::class.java)
                            packageManager.setComponentEnabledSetting(componentName,
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP)
                            SharedPrefUtil.editBoolPref(this, SHARED_PERF_FILE,
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

                        if (old.isSilent) {
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
                                JobManager.instance().cancelAllForTag("${SilenceJob.TAG}#${model.id}")
                            } else {
                                JobManager.instance().cancelAllForTag("${VibrateJob.TAG}#${model.id}")
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
                                val isEnabled = SharedPrefUtil.getBoolPref(this,
                                        SHARED_PERF_FILE, SILENCE_IS_ENABLE, true)
                                if (!isEnabled) {
                                    JobManager.instance().cancelAll()
                                    val componentName = ComponentName(this, BootReceiver::class.java)
                                    packageManager.setComponentEnabledSetting(componentName,
                                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                            PackageManager.DONT_KILL_APP)
                                    val id = SharedPrefUtil.getIntPref(this,
                                            SHARED_PERF_FILE, NOTIFICATION_SYNC_ID, 0)
                                    val intent = ServiceUtil().getServiceIndent("Silence",
                                            "Syncing all silence",
                                            id, this, PerformWork.PerformSyncJob::class.java)
                                    val alarm = getSystemService(Context.ALARM_SERVICE)
                                            as AlarmManager
                                    alarm.cancel(Util().alarmPendingIndent(this, intent))
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val whichPermission = call.argument<String>("permission")
                        when (whichPermission) {
                            "notification-policy" -> {
                                val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                                        NotificationManager
                                if (!nManager.isNotificationPolicyAccessGranted) {
                                    result.success("denied@")
                                    val intent = Intent(
                                            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    startActivity(intent)
                                } else {
                                    result.success("granted@")
                                }
                            }
                            "sms-service" -> {
                                val permissionPhoneState = Manifest.permission.READ_PHONE_STATE
                                val permissionSendSms = Manifest.permission.SEND_SMS
                                askPermission(arrayOf(permissionPhoneState, permissionSendSms),
                                        PERMISSION_SMS_SERVICE)
                            }
                            "contacts" -> {
                                val permissionPhoneState = Manifest.permission.READ_PHONE_STATE
                                val permissionReadContact = Manifest.permission.READ_CONTACTS
                                val permissionStorage = Manifest.permission.READ_EXTERNAL_STORAGE
                                askPermission(arrayOf(permissionPhoneState, permissionReadContact,
                                        permissionStorage), PERMISSION_CONTACT_SERVICE)
                            }
                        }
                    } else {
                        permissionCallback.sendResult("granted@")
                    }
                }
                "redirectPermissionSetting" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 0)
                }
                "queryContact" -> {
                    val query = call.argument<String>("query")
                    val intent = Intent()
                    intent.action = contactSearchAction
                    intent.putExtra("query", query)
                    localBroadcastManager.sendBroadcast(intent)
                }
                "whitelistOp" -> {
                    val op = call.argument<String>("op")
                    when (op) {
                        "insert" -> {
                            val phone = call.argument<String>("arg")
                            val uuid = call.argument<String>("uuid")
                            if (Patterns.PHONE.matcher(phone).matches()) {
                                SharedPrefUtil.editStringPref(this,
                                        SHARED_PERF_WHITE_LIST_FILE, uuid, phone)
                                result.success("success")
                            } else {
                                result.success("Invalid phone number")
                            }
                        }
                        "delete" -> {
                            val uuid = call.argument<String>("arg")
                            SharedPrefUtil.remove(this, SHARED_PERF_WHITE_LIST_FILE, uuid)
                            result.success("success")
                        }
                        "getAll" -> {
                            result.success(SharedPrefUtil.all(this,
                                    SHARED_PERF_WHITE_LIST_FILE))
                        }
                    }
                }
                else -> result.notImplemented()
            }
        }

        EventChannel(flutterView, channelEventService).setStreamHandler(
                object : EventChannel.StreamHandler {
                    var watcher: BroadcastReceiver? = null
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        watcher = serviceWatcherReceiver(events!!)
                        if (SharedPrefUtil.getBoolPref(this@MainActivity, SHARED_PERF_FILE
                                        , IS_SILENCE_ACTIVE, false)) {
                            events.success("service-started")
                        } else {
                            events.success("service-stopped")
                        }
                        val intentFilter = IntentFilter()
                        intentFilter.addAction(ForeService.SERVICE_STARTED_ACTION)
                        intentFilter.addAction(ForeService.STOP_SERVICE_ACTION)
                        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                        localBroadcastManager.registerReceiver(watcher, intentFilter)
                    }

                    override fun onCancel(arguments: Any?) {
                        localBroadcastManager.unregisterReceiver(watcher)
                        watcher = null
                    }
                }
        )

        EventChannel(flutterView, channelEventContacts).setStreamHandler(
                object : EventChannel.StreamHandler {
                    var watcher: BroadcastReceiver? = null
                    override fun onListen(arguments: Any?, event: EventChannel.EventSink?) {
                        watcher = contactLoader(event!!)
                        val intentFilter = IntentFilter()
                        intentFilter.addAction(contactSearchAction)
                        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
                        localBroadcastManager.registerReceiver(watcher, intentFilter)
                    }

                    override fun onCancel(arguments: Any?) {
                        localBroadcastManager.unregisterReceiver(watcher)
                        watcher = null
                    }

                }
        )
    }

    override fun onDestroy() {
        shouldStartBootReceiver()
        for (v in disposable.values) {
            if (!v.isDisposed)
                v.dispose()
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?,
                                            grantResults: IntArray?) {
        val grantedPermission = arrayListOf<String>()
        val neverAskPermission = arrayListOf<String>()
        if (grantResults!!.isNotEmpty()) {
            permissions!!.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(permission)) {
                            val isFirstTime = SharedPrefUtil.getBoolPref(this,
                                    SHARED_PERF_PERMISSION_FILE, permission, true)
                            if (isFirstTime) {
                                SharedPrefUtil.editBoolPref(this,
                                        SHARED_PERF_PERMISSION_FILE, permission, false)
                            } else {
                               neverAskPermission.add(permission)
                            }
                        }
                    }
                } else {
                    grantedPermission.add(permission)
                }
            }
            var result = "granted@"
            var first = true
            grantedPermission.forEach {
                if (first) {
                    first = false
                    result += it
                } else {
                    result += "^$it"
                }
            }
            result = "|setting@"
            first = true
            neverAskPermission.forEach {
                if (first) {
                    first = false
                    result += it
                } else {
                    result += "^$it"
                }
            }
            permissionCallback.sendResult(result)
        }
    }

    private fun askPermission(permissions: Array<String>, requestCode: Int) {
        val permissionPending = arrayListOf<String>()
        permissions.forEach {
            if (!Util().isPermissionGranted(this, it)) {
                permissionPending.add(it)
            }
        }
        if (permissionPending.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionPending.toTypedArray(),
                    requestCode)
        } else {
            var result = "granted@"
            permissions.forEach {
                result += "$it*"
            }
            permissionCallback.sendResult(result)
        }
    }
}
