package com.r42914lg.arkados.vitalk.utils

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import javax.inject.Inject

class PermissionsHelper @Inject constructor(
    private val appCompatActivity: AppCompatActivity,
    private val viTalkVM: ViTalkVM
) {
    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.CHANGE_NETWORK_STATE
    )
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        appCompatActivity.registerForActivityResult(RequestMultiplePermissions()) { result ->
            if (result.containsValue(false)) {
                viTalkVM.onPermissionsCheckFailed()
            } else {
                viTalkVM.onPermissionsCheckPassed()
            }
        }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.GET_ACCOUNTS
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                appCompatActivity,
                Manifest.permission.CHANGE_NETWORK_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viTalkVM.onPermissionsCheckPassed()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    init {
        checkPermissions()
    }
}