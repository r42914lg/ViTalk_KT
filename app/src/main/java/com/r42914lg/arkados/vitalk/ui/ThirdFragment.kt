package com.r42914lg.arkados.vitalk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.ViTalkConstants
import com.r42914lg.arkados.vitalk.controller.ThirdFragmentController
import com.r42914lg.arkados.vitalk.databinding.FragmentThirdBinding
import com.r42914lg.arkados.vitalk.graph.MyViewModelFactory
import com.r42914lg.arkados.vitalk.model.LocalVideo
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.model.VideoGalleryScanner
import javax.inject.Inject


class ThirdFragment : Fragment() {
    private var _binding: FragmentThirdBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var myViewModelFactory: MyViewModelFactory

    private lateinit var viTalkVM: ViTalkVM
    private lateinit var controller: ThirdFragmentController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.getActivityComponent()
            .inject(this)

        viTalkVM = ViewModelProvider(mainActivity, myViewModelFactory)[ViTalkVM::class.java]
        controller = ThirdFragmentController(viTalkVM)

        binding.videoGalleryRecycler.layoutManager = GridLayoutManager(
            context, ViTalkConstants.VIDEO_GALLERY_ADAPTER_COLUMNS
        )
        val videoGalleryScanner = VideoGalleryScanner(mainActivity)
        val videoGalleryAdapter = VideoGalleryAdapter(videoGalleryScanner, this)
        binding.videoGalleryRecycler.adapter = videoGalleryAdapter

        controller.initGalleryChooserFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun startVideoUploadNavigateToFirst(localVideoSelected: LocalVideo) {
        findNavController().navigate(R.id.action_ThirdFragment_to_FirstFragment)
        viTalkVM.localVideo = localVideoSelected
        viTalkVM.uiActionMutableLiveData.value = ViTalkVM.UPLOAD_LOCAL_VIDEO_CODE
    }
}