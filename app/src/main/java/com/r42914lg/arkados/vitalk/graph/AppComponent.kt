package com.r42914lg.arkados.vitalk.graph

import android.app.Application
import com.r42914lg.arkados.vitalk.model.FirebaseHelper
import com.r42914lg.arkados.vitalk.model.IDataLoaderListener
import com.r42914lg.arkados.vitalk.model.LocalStorageHelper
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataLoaderListenerModule::class, CoroutineScopeModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun exposeApplication(): Application
    fun exposeDataLoaderListener(): IDataLoaderListener
    fun exposeFactory(): MyViewModelFactory
    fun exposeVM(): ViTalkVM
    fun exposeFirebase(): FirebaseHelper
    fun exposeStorage(): LocalStorageHelper
}