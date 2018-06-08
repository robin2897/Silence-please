package com.inc.rims.silenceplease.dagger

import com.inc.rims.silenceplease.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
}