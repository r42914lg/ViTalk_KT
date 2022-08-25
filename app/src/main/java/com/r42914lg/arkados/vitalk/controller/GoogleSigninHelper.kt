package com.r42914lg.arkados.vitalk.controller

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

interface ISignInCallback {
    fun onSignIn(account: GoogleSignInAccount)
    fun onFailure(statusMessage: String)
}

class GoogleSignInHelper(
    appCompatActivity: AppCompatActivity,
    iSignInCallback: ISignInCallback
) {
    private var mGoogleSignInClient: GoogleSignInClient
    private var mAccountPicker: ActivityResultLauncher<Long>

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        mGoogleSignInClient = GoogleSignIn.getClient(appCompatActivity, gso)
        mAccountPicker = appCompatActivity.registerForActivityResult(
            object : ActivityResultContract<Long, Intent?>() {
                override fun createIntent(context: Context, input: Long): Intent {
                    return mGoogleSignInClient.signInIntent
                }
                override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
                    return intent
                }
            }
        ) { intent ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                iSignInCallback.onSignIn(account)
            } catch (e: ApiException) {
                iSignInCallback.onFailure(e.message + e.statusCode)
            }
        }
    }

    fun launchSignIn() {
        mAccountPicker.launch(999L)
    }
}