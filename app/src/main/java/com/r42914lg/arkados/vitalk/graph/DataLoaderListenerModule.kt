package com.r42914lg.arkados.vitalk.graph

import com.r42914lg.arkados.vitalk.model.DataLoaderListenerImpl
import com.r42914lg.arkados.vitalk.model.IDataLoaderListener
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DataLoaderListenerModule {
    @Singleton
    @Binds
    fun getDataLoaderListener(impl: DataLoaderListenerImpl): IDataLoaderListener
}