package com.r42914lg.arkados.vitalk.ui

interface IViTalkWorker {
    enum class Mode {
        SOURCE, RECORD, PREVIEW
    }

    val mode: Mode
    fun onRecordButtonEnabledFlag(aBoolean: Boolean)
    fun onRecordSessionEndedFlag(aBoolean: Boolean)
    fun onFirebaseUploadFinishedFlag(aBoolean: Boolean)
    fun navigateToWorkItems()
    fun onYouTubePlayHit()
}