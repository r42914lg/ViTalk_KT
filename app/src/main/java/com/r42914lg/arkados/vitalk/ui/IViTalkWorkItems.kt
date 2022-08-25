package com.r42914lg.arkados.vitalk.ui

import com.r42914lg.arkados.vitalk.model.WorkItemVideo

interface IViTalkWorkItems {
    fun onAddRowsToAdapter(workItemVideoList: List<WorkItemVideo>)
    fun notifyAdapterIconLoaded(position: Int)
    fun onFavoritesChanged()
}