package com.r42914lg.arkados.vitalk.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.tabs.TabLayout
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.controller.SecondFragmentController
import com.r42914lg.arkados.vitalk.databinding.FragmentSecondBinding
import com.r42914lg.arkados.vitalk.graph.MyViewModelFactory
import com.r42914lg.arkados.vitalk.media.MediaOrchestrator
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import javax.inject.Inject

class SecondFragment : Fragment(), IViTalkWorker {
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var myViewModelFactory: MyViewModelFactory

    private lateinit var viTalkVM: ViTalkVM
    private lateinit var controller: SecondFragmentController
    private lateinit var mediaOrchestrator: MediaOrchestrator
    private var currentTabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.getActivityComponent()
            .inject(this)

        viTalkVM = ViewModelProvider(mainActivity, myViewModelFactory)[ViTalkVM::class.java]
        controller = SecondFragmentController(viTalkVM)

        binding.shareLinkButton.isEnabled = false
        binding.shareAudioButton.isEnabled = false

        controller.initWorkerFragment(this, view.context)
        initTabsOneTwo()

        mediaOrchestrator = MediaOrchestrator(
            view.context, viewLifecycleOwner,
            binding.youtubePlayerView, binding.youtubePlayerSeekbar, viTalkVM, this
        )

        binding.muteSwitch.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            mediaOrchestrator.onMuteChecked(b)
        }

        binding.talkButton.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.talkButton.setBackgroundColor(Color.RED)
                binding.recordingIndicator.visibility = View.VISIBLE
                mediaOrchestrator.onRecordResume()
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                binding.talkButton.setBackgroundColor(
                    resources.getColor(
                        R.color.light_blue_900,
                        requireActivity().theme
                    )
                )
                binding.recordingIndicator.visibility = View.INVISIBLE
                mediaOrchestrator.onRecordPause()
            }
            false
        }

        viTalkVM.youtubeVideoIdToShareOrPreview = viTalkVM.currentYoutubeId

        binding.shareLinkButton.setOnClickListener {
            viTalkVM.uiActionMutableLiveData.value = ViTalkVM.SHARE_ACTION_CODE
        }

        binding.shareAudioButton.setOnClickListener {
            viTalkVM.uiActionMutableLiveData.value = ViTalkVM.AUDIO_ACTION_CODE
        }

        binding.finishRecordingButton.setOnClickListener {
            mediaOrchestrator.stopRecordingSession(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initTabsOneTwo() {
        val tab1 = binding.tabLayout.newTab()
        tab1.text = getString(R.string.tab1_title)
        binding.tabLayout.addTab(tab1)

        val tab2 = binding.tabLayout.newTab()
        tab2.text = getString(R.string.tab2_title)
        binding.tabLayout.addTab(tab2)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mediaOrchestrator.getMyUiController().animateButton(false)
                mediaOrchestrator.rewindVideo()
                currentTabPosition = tab.position
                when (currentTabPosition) {
                    0 -> tabOne()
                    1 -> tabTwo()
                    2 -> tabThree()
                    else -> throw IllegalStateException("Wrong TAB state --> $currentTabPosition")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initTabThree() {
        val tab3 = binding.tabLayout.newTab()
        tab3.text = getString(R.string.tab3_title)
        binding.tabLayout.addTab(tab3)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun prepareTab1(flag: Boolean) {
        binding.youtubePlayerSeekbar.setColor(if (flag) Color.RED else Color.GRAY)
        binding.youtubePlayerSeekbar.seekBar.setOnTouchListener { _: View?, _: MotionEvent? -> !flag }
    }

    private fun prepareTab2(flag: Boolean) {
        onRecordButtonEnabledFlag(false)
        binding.talkButton.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        binding.finishRecordingButton.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        if (flag) {
            mediaOrchestrator.releaseAudioPlayer()
        }
    }

    private fun prepareTab3(flag: Boolean) {
        binding.shareLinkButton.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        binding.shareAudioButton.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        binding.muteSwitch.visibility = if (flag) View.VISIBLE else View.INVISIBLE
        if (flag) {
            mediaOrchestrator.releaseAudioRecorder()
            mediaOrchestrator.initAudioPlayer()
            binding.muteSwitch.isChecked = false
        }
    }

    override val mode: IViTalkWorker.Mode
        get() = when (currentTabPosition) {
            0 -> IViTalkWorker.Mode.SOURCE
            1 -> IViTalkWorker.Mode.RECORD
            2 -> IViTalkWorker.Mode.PREVIEW
            else -> throw IllegalStateException("Current Tab position --> $currentTabPosition")
        }

    override fun onRecordButtonEnabledFlag(aBoolean: Boolean) {
        if (binding.tabLayout.selectedTabPosition != 1) {
            return
        }
        binding.talkButton.setBackgroundColor(
            if (aBoolean) resources.getColor(
                R.color.light_blue_900,
                activity!!.theme
            ) else Color.DKGRAY
        )
        binding.talkButton.isEnabled = aBoolean
        binding.finishRecordingButton.setBackgroundColor(
            if (aBoolean) resources.getColor(
                R.color.light_blue_900,
                activity!!.theme
            ) else Color.DKGRAY
        )
        binding.finishRecordingButton.isEnabled = aBoolean
    }

    override fun onRecordSessionEndedFlag(aBoolean: Boolean) {
        if (aBoolean) {
            if (binding.tabLayout.tabCount == 2) {
                initTabThree()
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2))
                tabThree()
                binding.muteSwitch.isChecked = false
                mediaOrchestrator.onMuteChecked(false)
            }
        } else {
            if (binding.tabLayout.tabCount == 3) {
                binding.tabLayout.removeTabAt(2)
            }
        }
    }

    override fun onFirebaseUploadFinishedFlag(aBoolean: Boolean) {
        binding.shareLinkButton.isEnabled = aBoolean
        binding.shareAudioButton.isEnabled = aBoolean
    }

    override fun navigateToWorkItems() {
        NavHostFragment.findNavController(this@SecondFragment)
            .navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    override fun onYouTubePlayHit() {
        if (currentTabPosition < 2 && binding.tabLayout.tabCount == 3) {
            binding.tabLayout.removeTabAt(2)
        }
    }

    override fun onResume() {
        super.onResume()
        mediaOrchestrator.onResumeFragment()
    }

    override fun onPause() {
        super.onPause()
        mediaOrchestrator.onPauseFragment()
    }

    private fun tabOne() {
        prepareTab1(true)
        prepareTab2(false)
        prepareTab3(false)
    }

    private fun tabTwo() {
        prepareTab1(false)
        prepareTab2(true)
        prepareTab3(false)
    }

    private fun tabThree() {
        prepareTab1(false)
        prepareTab2(false)
        prepareTab3(true)
    }
}