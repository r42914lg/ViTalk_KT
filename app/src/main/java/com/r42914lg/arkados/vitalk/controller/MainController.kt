package com.r42914lg.arkados.vitalk.controller

import android.content.DialogInterface
import android.content.Intent
import android.icu.text.MessageFormat
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.ViTalkConstants
import com.r42914lg.arkados.vitalk.model.LocalVideo
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.ui.ICoreFrame
import com.r42914lg.arkados.vitalk.utils.AudioProvider
import java.net.URLConnection

class MainController(
    private val appCompatActivity: AppCompatActivity,
    private val viTalkVM: ViTalkVM) : ISignInCallback {

    private val googleSignInHelper = GoogleSignInHelper(appCompatActivity, this)

    fun initMainActivity(iCoreFrame: ICoreFrame) {
        viTalkVM.favoritesLiveData.observe(appCompatActivity) { iCoreFrame.renderMenuItems() }
        viTalkVM.toastLiveData.observe(appCompatActivity) { s: String ->
            showToast(s)
        }
        viTalkVM.terminateDialogEventMutableLiveData.observe(appCompatActivity) { tdEvent ->
            showTerminateDialog(tdEvent.title, tdEvent.text)
        }
        viTalkVM.googleSignInLiveData.observe(appCompatActivity) { account: GoogleSignInAccount ->
            iCoreFrame.updateUI(account)
        }
        viTalkVM.liveToolBarTitle.observe(appCompatActivity) { s: String ->
            appCompatActivity.supportActionBar!!.title = s
        }
        viTalkVM.showFabLiveData.observe(appCompatActivity) { flag: Boolean ->
            iCoreFrame.showFab(flag)
        }
        viTalkVM.showTabOneMenuItems.observe(appCompatActivity) { flag: Boolean ->
            iCoreFrame.showTabOneMenuItems(flag)
        }
        viTalkVM.progressBarFlagLiveData.observe(appCompatActivity) { aBoolean: Boolean ->
            if (aBoolean) {
                iCoreFrame.startProgressOverlay()
            } else {
                iCoreFrame.stopProgressOverlay()
            }
        }
        viTalkVM.uiActionMutableLiveData.observe(appCompatActivity) { viTalkUIAction: Int ->
            when (viTalkUIAction) {
                ViTalkVM.UPLOAD_LOCAL_VIDEO_CODE -> startVideoUpload(viTalkVM.localVideo)
                ViTalkVM.SHARE_ACTION_CODE -> processShareRequest(viTalkVM.youtubeVideoIdToShareOrPreview)
                ViTalkVM.PREVIEW_ACTION_CODE -> processPreviewRequest(viTalkVM.youtubeVideoIdToShareOrPreview)
                ViTalkVM.ASK_RATINGS_ACTION_CODE -> (appCompatActivity as ICoreFrame).askRatings()
                ViTalkVM.GOOGLE_SIGNIN_ACTION_CODE -> doGoogleSignIn()
                ViTalkVM.AUDIO_ACTION_CODE -> {
                    val playAudioIntent = Intent()
                    val uri = Uri.parse(AudioProvider.CONTENT_URI.toString()
                            + "/" + ViTalkConstants.FILE_NAME)

                    playAudioIntent.action = Intent.ACTION_SEND
                    playAudioIntent.type = URLConnection.guessContentTypeFromName(uri.toString())
                    playAudioIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    appCompatActivity.startActivity(playAudioIntent)
                }
                else -> throw IllegalStateException("Wrong UI Action received in observer")
            }
        }
    }

    fun handleIntent(newIntent: Intent) {
        newIntent.type?.let { it ->
            if (it == "YOUTUBE_LINK") {
                newIntent.extras?.let {
                    val youTubeId = parseYouTubeId(it.getString(Intent.EXTRA_TEXT))
                    viTalkVM.addYouTubeIdToWorkItems(youTubeId)
                }
            }
            if (it == "VIDEO_URI") {
                val videoUri = newIntent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
                videoUri?.let { startVideoUpload(it) }
            }
            newIntent.type = "CONSUMED"
        }
        checkGoogleSignInUser()
    }

    private fun checkGoogleSignInUser() {
        val account = GoogleSignIn.getLastSignedInAccount(appCompatActivity)
        account?.let { onSignIn(it) }
    }

    fun doGoogleSignIn() {
        googleSignInHelper.launchSignIn()
    }

    fun noGoogleSignIn() = viTalkVM.googleSignInLiveData.value == null

    override fun onSignIn(account: GoogleSignInAccount) {
        viTalkVM.setGoogleAccount(account)
    }

    override fun onFailure(statusMessage: String) {
        showToast(statusMessage)
    }

    private fun showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(appCompatActivity, text, duration).show()
    }

    private fun processPreviewRequest(youTubeId: String) {
        val i = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                MessageFormat.format(
                    ViTalkConstants.URL_RESULT,
                    youTubeId,
                    viTalkVM.googleAccId()
                )
            )
        )
        appCompatActivity.startActivity(i)
    }

    private fun processShareRequest(youTubeId: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            MessageFormat.format(ViTalkConstants.URL_RESULT, youTubeId, viTalkVM.googleAccId())
        )
        sendIntent.type = "text/plain"
        appCompatActivity.startActivity(sendIntent)
    }

    private fun startVideoUpload(localVideoSelected: LocalVideo) {
        startVideoUpload(localVideoSelected.uri)
    }

    private fun startVideoUpload(videoUri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "video/3gpp"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Title_text")
        intent.putExtra(Intent.EXTRA_STREAM, videoUri)
        appCompatActivity.startActivity(
            Intent.createChooser(
                intent,
                appCompatActivity.getString(R.string.chooser_text)
            )
        )
    }

    private fun showTerminateDialog(title: String?, text: String?) {
        val dialog = AlertDialog.Builder(appCompatActivity).create()
        dialog.setTitle(title)
        dialog.setMessage(text)
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { d, _ ->
            appCompatActivity.finish()
            d.cancel()
        }
        dialog.setOnDismissListener { d ->
            appCompatActivity.finish()
            d.cancel()
        }
        dialog.show()
    }

    private fun parseYouTubeId(url: String?): String {
        val afterSlash = url!!.substring(url.lastIndexOf("/") + 1)
        val index = afterSlash.indexOf("?")
        return if (index == -1) afterSlash else afterSlash.substring(0, index)
    }
}