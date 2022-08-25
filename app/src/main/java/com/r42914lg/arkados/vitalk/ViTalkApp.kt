package com.r42914lg.arkados.vitalk

import android.app.Application
import com.r42914lg.arkados.vitalk.graph.AppComponent
import com.r42914lg.arkados.vitalk.graph.DaggerAppComponent

class ViTalkApp : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}