package com.r42914lg.arkados.vitalk.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MyViewModelFactory @Inject constructor(private val p_viTalkVM: Provider<ViTalkVM>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = if (modelClass == ViTalkVM::class.java)
            p_viTalkVM.get()
        else
            throw RuntimeException("unsupported view model class: $modelClass")

        return viewModel as T
    }
}