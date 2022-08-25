package com.r42914lg.arkados.vitalk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.controller.FirstFragmentController
import com.r42914lg.arkados.vitalk.databinding.FragmentFirstBinding
import com.r42914lg.arkados.vitalk.graph.MyViewModelFactory
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.model.WorkItemVideo
import java.util.ArrayList
import javax.inject.Inject

class FirstFragment : Fragment(), IViTalkWorkItems {
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    lateinit var myViewModelFactory: MyViewModelFactory

    private lateinit var viTalkVM: ViTalkVM
    private lateinit var controller: FirstFragmentController

    private lateinit var adapter: WorkItemAdapter
    private val adapterVideoList = ArrayList<WorkItemVideo>()
    private val adapterVideoListFiltered = ArrayList<WorkItemVideo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        mainActivity.getActivityComponent()
            .inject(this)

        viTalkVM = ViewModelProvider(mainActivity, myViewModelFactory)[ViTalkVM::class.java]
        controller = FirstFragmentController(viTalkVM)

        binding.workItemRecycler.layoutManager = LinearLayoutManager(context)

        adapter = WorkItemAdapter(adapterVideoListFiltered, this)
        binding.workItemRecycler.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(SwipeToDelete(adapter))
        itemTouchHelper.attachToRecyclerView(binding.workItemRecycler)

        controller.initWorkItemFragment(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setVideoIdForWorkNavigateToSecond(youTubeId: String) {
        if (viTalkVM.isOnline) {
            controller.setVideoIdForWork(youTubeId)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        } else {
            viTalkVM.notifyUIShowToast("Check your internet connection and retry")
        }
    }

    private fun doFilterQuizzes() {
        adapterVideoListFiltered.clear()

        if (viTalkVM.needToFilter()) {
            val favorites = viTalkVM.favoriteIDs
            for (workItemVideo in adapterVideoList) {
                if (favorites.contains(workItemVideo.youTubeId)) {
                    adapterVideoListFiltered.add(workItemVideo)
                }
            }
        } else {
            adapterVideoListFiltered.addAll(adapterVideoList)
        }
    }

    override fun onAddRowsToAdapter(workItemVideoList: List<WorkItemVideo>) {
        adapterVideoList.clear()
        adapterVideoList.addAll(workItemVideoList)
        onFavoritesChanged()
    }

    override fun notifyAdapterIconLoaded(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun onFavoritesChanged() {
        doFilterQuizzes()
        adapter.notifyDataSetChanged()
    }
}