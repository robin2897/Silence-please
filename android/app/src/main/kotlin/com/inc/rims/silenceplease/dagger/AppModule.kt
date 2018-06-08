package com.inc.rims.silenceplease.dagger

import com.evernote.android.job.JobManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideGson(): Gson {
        return GsonBuilder().serializeNulls().create()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideJobManager(): JobManager {
        return JobManager.instance()
    }
}