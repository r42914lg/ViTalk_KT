package com.r42914lg.arkados.vitalk.media

import android.media.MediaPlayer
import java.io.IOException

class AudioPlayer : MediaPlayer() {
    private var endOfAudio = false

    fun initAudioPlayer(dataSource: String?) {
        try {
            setDataSource(dataSource)
            prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun start() {
        if (endOfAudio) {
            return
        }
        super.start()
    }

    fun clearEndOfAudioFlag() {
        endOfAudio = false
    }

    init {
        setOnCompletionListener {
            endOfAudio = true
        }
    }
}