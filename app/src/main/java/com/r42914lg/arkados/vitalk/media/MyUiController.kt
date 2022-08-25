package com.r42914lg.arkados.vitalk.media

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.widget.Button
import androidx.core.content.ContextCompat
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.ui.IViTalkWorker

class MyUiController(
    private val context: Context,
    customPlayerUi: View,
    private val youTubePlayer: YouTubePlayer,
    private val orchestrator: MediaOrchestrator,
    private val iViTalkWorker: IViTalkWorker) : AbstractYouTubePlayerListener() {

    private lateinit var panel: View
    private val anim: ObjectAnimator
    private var animStarted = false
    private lateinit var playPauseButton: Button
    private val playerTracker: YouTubePlayerTracker = YouTubePlayerTracker()

    init {
        youTubePlayer.addListener(playerTracker)
        initViews(customPlayerUi)

        anim = ObjectAnimator.ofFloat(playPauseButton, View.ALPHA, 0.25f, 1.0f)
        anim.duration = 1500
        anim.repeatCount = Animation.INFINITE
    }

    private fun initViews(playerUi: View) {
        panel = playerUi.findViewById(R.id.panel)
        playPauseButton = playerUi.findViewById(R.id.play_pause_button)

        playPauseButton.setOnClickListener {
            if (iViTalkWorker.mode == IViTalkWorker.Mode.RECORD && !orchestrator.recorderReady()) {
                orchestrator.initAudioRecorder()
            }

            iViTalkWorker.onYouTubePlayHit()

            if (checkIfPlaying()) {
                animateButton(false)
                iViTalkWorker.onRecordButtonEnabledFlag(false)
                youTubePlayer.pause()

                if (iViTalkWorker.mode == IViTalkWorker.Mode.RECORD) {
                    orchestrator.audioRecorder!!.pause()
                }

                if (iViTalkWorker.mode == IViTalkWorker.Mode.PREVIEW) {
                    orchestrator.audioPlayer!!.pause()
                }
            } else {
                animateButton(true)
                iViTalkWorker.onRecordButtonEnabledFlag(true)
                youTubePlayer.play()

                if (iViTalkWorker.mode == IViTalkWorker.Mode.RECORD) {
                    youTubePlayer.mute()
                    orchestrator.audioRecorder!!.setMicMuted(true)
                    orchestrator.audioRecorder!!.resumeOrStart()
                }

                if (iViTalkWorker.mode == IViTalkWorker.Mode.PREVIEW) {
                    youTubePlayer.setVolume(1)
                    orchestrator.audioPlayer!!.start()
                }
            }
        }
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
        if (state == PlayerState.PLAYING || state == PlayerState.PAUSED || state == PlayerState.VIDEO_CUED) {
            panel.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        } else if (state == PlayerState.BUFFERING) {
            panel.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        }
    }

    fun animateButton(flag: Boolean) {
        if (flag) {
            if (animStarted)
                anim.resume()
            else {
                anim.start()
                animStarted = true
            }
        } else {
            anim.pause()
            anim.setCurrentFraction(1.0f)
        }
    }

    fun endAnimation() {
        anim.end()
    }

    fun resumeAnimation() {
        if (checkIfPlaying()) {
            animateButton(true)
        }
    }

    private fun checkIfPlaying(): Boolean {
        return playerTracker.state == PlayerState.PLAYING
    }
}