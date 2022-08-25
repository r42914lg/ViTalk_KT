package com.r42914lg.arkados.vitalk.controller

import androidx.fragment.app.Fragment
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.ui.IViTalkWorkItems

class FirstFragmentController(private val viTalkVM: ViTalkVM) {

    fun initWorkItemFragment(iViTalkWorkItems: IViTalkWorkItems) {
        viTalkVM.showFabLiveData.value = true
        viTalkVM.showTabOneMenuItems.value = true
        viTalkVM.progressBarFlagLiveData.value = false

        viTalkVM.workItemsLoadedFlagLiveData
            .observe((iViTalkWorkItems as Fragment).viewLifecycleOwner) { aBoolean: Boolean ->
            if (aBoolean) {
                iViTalkWorkItems.onAddRowsToAdapter(viTalkVM.workItemVideoList)
            }
        }

        viTalkVM.favoritesLiveData
            .observe((iViTalkWorkItems as Fragment).viewLifecycleOwner) {
                iViTalkWorkItems.onFavoritesChanged()
            }

        viTalkVM.invalidateItemAtPositionLiveData
            .observe((iViTalkWorkItems as Fragment).viewLifecycleOwner) { position: Int ->
            iViTalkWorkItems.notifyAdapterIconLoaded(
                position
            )
        }

        viTalkVM.requestToolbarUpdate()
    }

    fun setVideoIdForWork(youTubeId: String) {
        viTalkVM.onVideIdSelected(youTubeId)
        viTalkVM.recordSessionEndedFlagLiveData.value = false
    }
}