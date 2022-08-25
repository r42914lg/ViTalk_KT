package com.r42914lg.arkados.vitalk.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import java.io.IOException

class AudioRecorderWithMute(private val context: Context, private val audioFileName: String) :
    MediaRecorder(context) {

    private enum class State {
        ZERO, READY, STARTED, PAUSED, RESUMED, STOPPED
    }

    private var currentState: State = State.ZERO

    fun initAudioRecorder() {
        if (currentState == State.READY) {
            return
        }
        if (currentState != State.ZERO) {
            reset()
            currentState = State.ZERO
        }
        setAudioSource(AudioSource.MIC)
        setOutputFormat(OutputFormat.AAC_ADTS)
        setAudioEncoder(AudioEncoder.AAC)
        setOutputFile(audioFileName)
        try {
            prepare()
            currentState = State.READY
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setMicMuted(state: Boolean) {
        val myAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val workingAudioMode = myAudioManager.mode
        myAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        if (myAudioManager.isMicrophoneMute != state) {
            myAudioManager.isMicrophoneMute = state
        }

        myAudioManager.mode = workingAudioMode
    }

    private fun wasStarted(): Boolean {
        return currentState == State.STARTED || currentState == State.STOPPED || currentState == State.PAUSED || currentState == State.RESUMED
    }

    fun resumeOrStart() {
        if (wasStarted()) {
            resume()
        } else {
            start()
        }
    }

    override fun resume() {
        super.resume()
        currentState = State.RESUMED
    }

    override fun start() {
        super.start()
        currentState = State.STARTED
    }

    override fun stop() {
        super.stop()
        currentState = State.STOPPED
    }

    override fun pause() {
        super.pause()
        currentState = State.PAUSED
    }

    override fun reset() {
        super.reset()
        currentState = State.ZERO
    }
}