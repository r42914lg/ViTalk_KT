package com.r42914lg.arkados.vitalk.media

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.ViTalkConstants
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.ui.IViTalkWorker

class MediaOrchestrator(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val youTubePlayerView: YouTubePlayerView,
    private val youTubePlayerSeekBar: YouTubePlayerSeekBar,
    private val viTalkVM: ViTalkVM,
    private val iViTalkWorker: IViTalkWorker
) {

    private var recorderReady = false
    private var controllerReady = false
    private val dataSource: String =
        context.externalCacheDir!!.absolutePath + "/" + ViTalkConstants.FILE_NAME

    var audioRecorder: AudioRecorderWithMute? = null
    var audioPlayer: AudioPlayer? = null

    private lateinit var youTubePlayer: YouTubePlayer
    private lateinit var myUiController: MyUiController

    init {
        viTalkVM.dataSource = dataSource
        initVideoPlayer()
    }

    fun initAudioRecorder() {
        audioRecorder = AudioRecorderWithMute(context, dataSource)
        audioRecorder?.initAudioRecorder().also { recorderReady = true }
    }

    fun initAudioPlayer() {
        audioPlayer = AudioPlayer()
        audioPlayer?.initAudioPlayer(dataSource)
    }

    fun recorderReady(): Boolean {
        return recorderReady
    }

    fun releaseAudioRecorder() {
        audioRecorder?.release().also { recorderReady = false }
    }

    fun releaseAudioPlayer() {
        audioPlayer?.release()
    }

    private fun initVideoPlayer() {
        lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.initialize(
            object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    this@MediaOrchestrator.youTubePlayer = youTubePlayer
                    myUiController = MyUiController(
                        context,
                        youTubePlayerView.inflateCustomPlayerUi(R.layout.my_player_ui),
                        youTubePlayer,
                        this@MediaOrchestrator,
                        iViTalkWorker
                    )
                    controllerReady = true
                    youTubePlayer.addListener(myUiController)
                    youTubePlayer.addListener(youTubePlayerSeekBar)
                    youTubePlayerSeekBar.youtubePlayerSeekBarListener =
                        object : YouTubePlayerSeekBarListener {
                            override fun seekTo(time: Float) {
                                youTubePlayer.seekTo(time)
                            }
                        }

                    youTubePlayer.cueVideo(viTalkVM.currentYoutubeId, 0f)
                    viTalkVM.onYouTubePlayerReady()
                }

                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
                    if (state == PlayerState.VIDEO_CUED) {
                        viTalkVM.onVideoCued()
                    }
                    if (state == PlayerState.ENDED) {
                        if (iViTalkWorker.mode == IViTalkWorker.Mode.RECORD) {
                            stopRecordingSession(false)
                        }
                        if (iViTalkWorker.mode == IViTalkWorker.Mode.PREVIEW) {
                            audioPlayer?.clearEndOfAudioFlag()
                        }
                        myUiController.animateButton(false)
                    }
                }
            }, IFramePlayerOptions.Builder().controls(0).build()
        )
    }

    fun rewindVideo() {
        youTubePlayer.seekTo(0f)
        youTubePlayer.pause()
    }

    fun onMuteChecked(b: Boolean) {
        if (b) {
            youTubePlayer.mute()
        } else {
            youTubePlayer.unMute()
        }
    }

    fun onRecordResume() {
        audioRecorder?.setMicMuted(false)
    }

    fun onRecordPause() {
        audioRecorder?.setMicMuted(true)
    }

    fun onResumeFragment() {
        if (!controllerReady)
            return

        myUiController.resumeAnimation()
        initAudioPlayer()
    }

    fun onPauseFragment() {
        if (!controllerReady)
            return

        myUiController.endAnimation()
        releaseAudioPlayer()
        releaseAudioRecorder()
    }

    fun stopRecordingSession(forceYoutubeStopFlag: Boolean) {
        if (forceYoutubeStopFlag) {
            youTubePlayer.pause()
        }
        audioRecorder?.stop()
        viTalkVM.onRecordSessionEnded(dataSource)
    }

    fun getMyUiController(): MyUiController = myUiController
}