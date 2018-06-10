package com.inc.rims.silenceplease

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.arch.persistence.db.SupportSQLiteQueryBuilder
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.evernote.android.job.JobManager
import com.google.gson.Gson
import com.inc.rims.silenceplease.room.DataDatabase
import com.inc.rims.silenceplease.room.DataModel
import com.inc.rims.silenceplease.room.JsonArrayDataModel
import com.inc.rims.silenceplease.util.Validation
import com.inc.rims.silenceplease.util.PerformWork
import com.inc.rims.silenceplease.worker.RingerJob
import com.inc.rims.silenceplease.worker.SilenceJob
import com.inc.rims.silenceplease.worker.VibrateJob
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject



class MainActivity : FlutterActivity() {

    private val channel = "com.inc.rims.silenceplease/database"
    private val tag = MainActivity::class.simpleName
    private val disposable = mutableMapOf<String, Disposable>()

    @Inject
    lateinit var gson: Gson

    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
        (application as BaseApp).getAppComponent().inject(this)
        val db = DataDatabase.getInstance(this)!!
        firstRun()
        askPermissions()

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

                        val isFirstInsert = getSharedPreferences("FlutterSharedPreferences",
                                0).getBoolean("isFirstInsert",
                                true)
                        if (isFirstInsert) {
                            PerformWork().startDailySync()
                            getSharedPreferences("FlutterSharedPreferences", 0).edit()
                                    .putBoolean("isFirstInsert", false).commit()
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
                                    .cancelAllForTag("${SilenceJob.TAG}-${model.id}")
                        } else {
                            JobManager.instance()
                                    .cancelAllForTag("${VibrateJob.TAG}-${model.id}")
                        }
                        JobManager.instance().cancelAllForTag("${RingerJob.TAG}-${model.id}")
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
                                        cancelAllForTag("${SilenceJob.TAG}-${model.id}")
                            } else {
                                JobManager.instance().
                                        cancelAllForTag("${VibrateJob.TAG}-${model.id}")
                            }
                        }
                        JobManager.instance().cancelAllForTag("${RingerJob.TAG}-${model.id}")
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
                                val isEnabled = getSharedPreferences(
                                        "FlutterSharedPreferences", 0)
                                        .getBoolean("IS_ENABLE", true)
                                if (!isEnabled) {
                                    JobManager.instance().cancelAll()
                                } else {
                                    val models = db.dataModelDao().getAll().blockingGet()
                                    if (models?.size != 0) {
                                        PerformWork().startDailySync()
                                        PerformWork().performSync()
                                    }
                                }
                            }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun askPermissions() {
        Completable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val n = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                        if (n.isNotificationPolicyAccessGranted) {
                            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                        } else {
                            val intent =
                                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            startActivity(intent)
                        }
                    }
                }
    }

    override fun onDestroy() {
        for (v in disposable.values) {
            if (!v.isDisposed)
                v.dispose()
        }
        super.onDestroy()
    }

    @SuppressLint("ApplySharedPref")
    private fun firstRun() {
        val isFirst = getSharedPreferences("FlutterSharedPreferences", 0)
                .getBoolean("isFirst", true)
        if (isFirst) {
            getSharedPreferences("FlutterSharedPreferences", 0).edit()
                    .putBoolean("isFirst", false).commit()
            getSharedPreferences("FlutterSharedPreferences", 0).edit()
                    .putInt("notifyId", 1).commit()
        }
    }
}
