package com.r42914lg.arkados.vitalk.controller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import com.r42914lg.arkados.vitalk.model.RetryDialogEvent
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.ui.IViTalkWorker

class SecondFragmentController(private val viTalkVM: ViTalkVM) {

    fun initWorkerFragment(iWorkerFragment: IViTalkWorker, context: Context) {
        viTalkVM.showFabLiveData.value = false
        viTalkVM.showTabOneMenuItems.value = false
        viTalkVM.recordSessionEndedFlagLiveData
            .observe((iWorkerFragment as Fragment).viewLifecycleOwner) { aBoolean: Boolean ->
            iWorkerFragment.onRecordSessionEndedFlag(
                aBoolean
            )
        }
        viTalkVM.firebaseUploadFinishedLiveData
            .observe((iWorkerFragment as Fragment).viewLifecycleOwner) { aBoolean: Boolean ->
            iWorkerFragment.onFirebaseUploadFinishedFlag(aBoolean)
            viTalkVM.uiActionMutableLiveData.setValue(ViTalkVM.ASK_RATINGS_ACTION_CODE)
        }
        viTalkVM.dialogEventMutableLiveData
            .observe((iWorkerFragment as Fragment).viewLifecycleOwner) { dialogEvent: RetryDialogEvent ->
            showRetryUploadDialog(
                dialogEvent.title,
                dialogEvent.text,
                iWorkerFragment,
                context
            )
        }
    }

    private fun showRetryUploadDialog(
        title: String,
        text: String,
        iViTalkWorker: IViTalkWorker,
        context: Context) {

        val dialog = AlertDialog.Builder(context).create()
        dialog.setTitle(title)
        dialog.setMessage(text)

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES") { d, _ ->
            viTalkVM.onRecordSessionEnded(viTalkVM.dataSource)
            d.cancel()
        }

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO") { d, _ ->
            iViTalkWorker.navigateToWorkItems()
            d.cancel()
        }

        dialog.setOnDismissListener { d ->
            iViTalkWorker.navigateToWorkItems()
            d.cancel()
        }

        dialog.show()
    }
}