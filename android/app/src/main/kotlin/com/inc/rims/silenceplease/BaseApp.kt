package com.inc.rims.silenceplease

import com.evernote.android.job.JobManager
import com.inc.rims.silenceplease.dagger.AppComponent
import com.inc.rims.silenceplease.dagger.DaggerAppComponent
import com.inc.rims.silenceplease.util.SilenceJobCreator
import io.flutter.app.FlutterApplication

class BaseApp: FlutterApplication() {

    private lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.create()
        JobManager.create(this).addJobCreator(SilenceJobCreator())
    }

    fun getAppComponent(): AppComponent = component
}