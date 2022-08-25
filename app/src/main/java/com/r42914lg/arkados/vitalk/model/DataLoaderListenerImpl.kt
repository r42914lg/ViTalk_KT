package com.r42914lg.arkados.vitalk.model
import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DataLoaderListenerImpl @Inject constructor(private val viTalkVM: Provider<ViTalkVM>) :
    IDataLoaderListener {

    override fun callbackLoadImageFromURL(youTubeImage: Bitmap, youTubeId: String) {
        viTalkVM.get().imagesMap[youTubeId] = youTubeImage
        val positionInAdapter = viTalkVM.get().lookUpForPositionInAdapter(youTubeId)
        if (positionInAdapter >= 0) {
            viTalkVM.get().invalidateItemAtPositionLiveData.value = positionInAdapter
        }
    }

    override fun callbackVideoTileReceived(title: String, youTubeId: String) {
        val index = viTalkVM.get().lookUpForIndexInList(youTubeId)
        if (index != -1) {
            viTalkVM.get().workItemVideoList[index].title = title
            val positionInAdapter = viTalkVM.get().lookUpForPositionInAdapter(youTubeId)
            if (positionInAdapter >= 0) {
                viTalkVM.get().invalidateItemAtPositionLiveData.value = positionInAdapter
            }
        }
    }

    override fun callbackFirebaseAuthenticated() {
        viTalkVM.get().firebaseAuthenticated = true
    }

    override fun onFirebaseUploadFailed(fullPath: String) {
        viTalkVM.get().progressBarFlagLiveData.value = false
        viTalkVM.get().retryDialog()
    }

    override fun onFirebaseUploadFinished(youTubeId: String) {
        viTalkVM.get().setRecordExistFlag(youTubeId, true)
        viTalkVM.get().storeWorkItems()
        viTalkVM.get().progressBarFlagLiveData.value = false
        viTalkVM.get().notifyUIShowToastOnUploadFinished()
        viTalkVM.get().firebaseUploadFinishedLiveData.value = true
    }
}