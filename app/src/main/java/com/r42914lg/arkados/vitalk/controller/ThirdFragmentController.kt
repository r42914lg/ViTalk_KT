package com.r42914lg.arkados.vitalk.controller

import com.r42914lg.arkados.vitalk.model.ViTalkVM

class ThirdFragmentController(private val viTalkVM: ViTalkVM) {
    fun initGalleryChooserFragment() {
        viTalkVM.showFabLiveData.value = false
        viTalkVM.showTabOneMenuItems.value = false
    }
}