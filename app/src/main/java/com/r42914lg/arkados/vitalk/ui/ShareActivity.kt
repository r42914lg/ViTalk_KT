package com.r42914lg.arkados.vitalk.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class ShareActivity : AppCompatActivity() {

    override fun onNewIntent(_intent: Intent) {
        super.onNewIntent(_intent)
        intent = _intent
    }

    override fun onResume() {
        super.onResume()
        val action = intent.action
        val type = intent.type
        val extras = intent.extras
        val i = Intent(this, MainActivity::class.java)
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                i.type = "YOUTUBE_LINK"
                i.putExtras(extras!!)
            }
            if ("video/mp4" == type) {
                i.type = "VIDEO_URI"
                i.putExtras(extras!!)
            }
        }
        startActivity(i)
        finish()
    }
}