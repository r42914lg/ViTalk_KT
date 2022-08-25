package com.r42914lg.arkados.vitalk.model

import android.graphics.Bitmap

interface IDataLoaderListener {
    fun callbackLoadImageFromURL(youTubeImage: Bitmap, youTubeId: String)
    fun callbackVideoTileReceived(title: String, youTubeId: String)
    fun callbackFirebaseAuthenticated()
    fun onFirebaseUploadFailed(fullPath: String)
    fun onFirebaseUploadFinished(youTubeId: String)
}