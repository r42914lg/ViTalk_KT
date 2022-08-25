package com.r42914lg.arkados.vitalk.graph

import androidx.appcompat.app.AppCompatActivity
import com.r42914lg.arkados.vitalk.ui.*
import dagger.BindsInstance
import dagger.Component

@ScreenScope
@Component(dependencies = [AppComponent::class])
interface ActivityComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(fragment: FirstFragment)
    fun inject(fragment: SecondFragment)
    fun inject(fragment: ThirdFragment)
    fun inject(adapter: WorkItemAdapter)

    @Component.Factory
    interface Factory {
        fun create(
            appComponent: AppComponent,
            @BindsInstance activity: AppCompatActivity
        ): ActivityComponent
    }
}