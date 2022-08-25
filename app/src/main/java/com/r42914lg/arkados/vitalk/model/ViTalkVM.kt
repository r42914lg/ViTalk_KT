package com.r42914lg.arkados.vitalk.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.graphics.Bitmap
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.r42914lg.arkados.vitalk.R
import java.io.Closeable
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViTalkVM @Inject constructor(
    private val app: Application,
    private val firebaseHelper: FirebaseHelper,
    private val localStorageHelper: LocalStorageHelper,
) : AndroidViewModel(app) {

    companion object {
        const val UPLOAD_LOCAL_VIDEO_CODE = 0
        const val SHARE_ACTION_CODE = 1
        const val PREVIEW_ACTION_CODE = 2
        const val ASK_RATINGS_ACTION_CODE = 3
        const val GOOGLE_SIGNIN_ACTION_CODE = 4
        const val AUDIO_ACTION_CODE = 5
    }

    lateinit var currentYoutubeId: String
    lateinit var youtubeVideoIdToShareOrPreview: String
    lateinit var dataSource: String
    lateinit var localVideo: LocalVideo

    var isOnline = false
    var firebaseAuthenticated = false

    val toastLiveData = MutableLiveData<String>()
    private val youtubeVideoIdLiveData = MutableLiveData<String>()
    val progressBarFlagLiveData = MutableLiveData<Boolean>()
    val recordSessionEndedFlagLiveData = MutableLiveData<Boolean>()
    val favoritesLiveData = MutableLiveData<FavoritesEvent>()
    val dialogEventMutableLiveData = MutableLiveData<RetryDialogEvent>()
    val terminateDialogEventMutableLiveData = MutableLiveData<TerminateDialogEvent>()
    val workItemsLoadedFlagLiveData = MutableLiveData<Boolean>()
    val invalidateItemAtPositionLiveData = MutableLiveData<Int>()
    val firebaseUploadFinishedLiveData = MutableLiveData<Boolean>()
    val googleSignInLiveData = MutableLiveData<GoogleSignInAccount>()
    val liveToolBarTitle = MutableLiveData<String>()
    val showFabLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val showTabOneMenuItems = MutableLiveData<Boolean>()
    val uiActionMutableLiveData = MutableLiveData<Int>()

    val imagesMap: MutableMap<String, Bitmap>
    val favoriteIDs: MutableSet<String>
    val workItemVideoList: MutableList<WorkItemVideo>

    init {
        imagesMap = Hashtable()

        workItemVideoList = localStorageHelper.loadWorkItems()
        workItemsLoadedFlagLiveData.value = true
        favoriteIDs = localStorageHelper.loadFavorites()
    }

    override fun onCleared() {
        super.onCleared()
        localStorageHelper.cancelCS()
    }

    fun notifyUIShowToast(text: String) {
        toastLiveData.value = text
    }

    fun lookupForBitmap(imageId: String?): Bitmap? {
        return imagesMap[imageId]
    }

    fun checkImageLoaded(imageId: String?): Boolean {
        return imagesMap.containsKey(imageId)
    }

    fun onYouTubePlayerReady() {
        youtubeVideoIdLiveData.value = currentYoutubeId
    }

    fun onVideoCued() {
        progressBarFlagLiveData.value = false
    }

    private fun notifyFavoritesExist(doEnable: Boolean) {
        val event = favoritesLiveData.value!!
        event.enableFavorites = doEnable

        if (!doEnable) {
            event.favoritesChecked = false
        }
        favoritesLiveData.value = event
    }

    fun setGoogleAccount(credential: GoogleSignInAccount) {
        googleSignInLiveData.value = credential
        liveToolBarTitle.value =
            app.getString(R.string.first_fragment_label) + " - " + credential.displayName
    }

    fun requestToolbarUpdate() {
        val account = googleSignInLiveData.value
        if (account != null) {
            liveToolBarTitle.value =
                app.getString(R.string.first_fragment_label) + " - " + account.displayName
        }
    }

    fun noGoogleSignIn(): Boolean {
        return googleSignInLiveData.value == null
    }

    fun checkIfFavorite(quizId: String?): Boolean {
        return favoriteIDs.contains(quizId)
    }

    private fun crossCheckFavorites(): Boolean {
        for (workItemVideo in workItemVideoList) {
            if (favoriteIDs.contains(workItemVideo.youTubeId)) {
                return true
            }
        }
        return false
    }

    fun processFavoriteAdded(quizId: String) {
        favoriteIDs.add(quizId)
        localStorageHelper.storeFavorites(favoriteIDs)
        notifyFavoritesExist(true)
    }

    fun processFavoriteRemoved(quizId: String) {
        favoriteIDs.remove(quizId)
        localStorageHelper.storeFavorites(favoriteIDs)
        notifyFavoritesExist(crossCheckFavorites())
    }

    fun setFavoritesChecked(favoritesChecked: Boolean) {
        favoritesLiveData.value?.apply {
            this.favoritesChecked = favoritesChecked
            favoritesLiveData.value = this
        }
    }

    fun onPermissionsCheckPassed() {}
    fun onPermissionsCheckFailed() {
        terminateDialogEventMutableLiveData.value = TerminateDialogEvent(
            app.getString(R.string.dialog_terminate_no_permissions_title),
            app.getString(R.string.dialog_terminate_no_permissions_text)
        )
    }

    fun setNetworkStatus(isOnline: Boolean) {
        this.isOnline = isOnline
        if (isOnline) {
            loadYoutubeThumbnailsAndTitles()
        }
    }

    fun onVideIdSelected(youtubeVideoId: String) {
        currentYoutubeId = youtubeVideoId
        setRecordExistFlag(youtubeVideoId, false)
        progressBarFlagLiveData.value = true
    }

    fun onRecordSessionEnded(dataSource: String?) {
        recordSessionEndedFlagLiveData.value = true
        if (!firebaseAuthenticated) {
            terminateDialogEventMutableLiveData.value = TerminateDialogEvent(
                app.getString(R.string.dialog_terminate_no_firebase_title),
                app.getString(R.string.dialog_terminate_no_firebase_text)
            )
        }
        firebaseHelper.uploadAudioWithId(
            currentYoutubeId,
            googleSignInLiveData.value?.id!!,
            dataSource!!
        )
        progressBarFlagLiveData.value = true
    }

    fun addYouTubeIdToWorkItems(youtubeVideoId: String) {
        if (lookUpForPositionInAdapter(youtubeVideoId) == -2) {
            workItemVideoList.add(WorkItemVideo(false, youtubeVideoId))
            localStorageHelper.loadImageFromURL(youtubeVideoId)
            localStorageHelper.queryYouTubeTitleFromURL(youtubeVideoId)
            localStorageHelper.storeWorkItems(workItemVideoList)
            workItemsLoadedFlagLiveData.setValue(true)
        } else {
            notifyUIShowToast(app.getString(R.string.toast_video_exists) + youtubeVideoId)
        }
    }

    fun onWorkItemDeleted(youtubeVideoId: String?) {
        val index = lookUpForIndexInList(youtubeVideoId)
        workItemVideoList.removeAt(index)
        localStorageHelper.storeWorkItems(workItemVideoList)
    }

    fun lookUpForPositionInAdapter(youTubeId: String?): Int {
        for (i in workItemVideoList.indices) {
            if (workItemVideoList[i].youTubeId == youTubeId) {
                return workItemVideoList[i].positionInAdapter
            }
        }
        return -2
    }

    fun lookUpForIndexInList(youTubeId: String?): Int {
        for (i in workItemVideoList.indices) {
            if (workItemVideoList[i].youTubeId == youTubeId) {
                return i
            }
        }
        return -1
    }

    private fun loadYoutubeThumbnailsAndTitles() {
        if (isOnline && workItemVideoList.isNotEmpty()) {
            for (workItemVideo in workItemVideoList) {
                if (!imagesMap.containsKey(workItemVideo.youTubeId)) {
                    localStorageHelper.loadImageFromURL(workItemVideo.youTubeId)
                    localStorageHelper.queryYouTubeTitleFromURL(workItemVideo.youTubeId)
                }
            }
        }
    }

    fun setRecordExistFlag(youtubeVideoId: String?, flag: Boolean) {
        for (workItemVideo in workItemVideoList) {
            if (workItemVideo.youTubeId == youtubeVideoId) {
                workItemVideo.recordExists = flag
                return
            }
        }
    }

    fun retryDialog() {
        dialogEventMutableLiveData.value = RetryDialogEvent(
            app.getString(R.string.dialog_upload_failed_title),
            app.getString(R.string.dialog_upload_failed_text)
        )
    }

    fun storeWorkItems() {
        localStorageHelper.storeWorkItems(workItemVideoList)
    }

    fun notifyUIShowToastOnUploadFinished() {
        notifyUIShowToast(app.getString(R.string.toast_upload_finished))
    }

    fun needToFilter(): Boolean {
        favoritesLiveData.value?.apply {
            return this.checkedAndEnabled
        }
        return false
    }

    fun googleAccId() = googleSignInLiveData.value?.id;
}