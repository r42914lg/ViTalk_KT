package com.r42914lg.arkados.vitalk.ui

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.graph.MyViewModelFactory
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.model.WorkItemVideo
import javax.inject.Inject

class WorkItemAdapter(
    private val videoList: MutableList<WorkItemVideo>,
    private val firstFragment: FirstFragment
) : RecyclerView.Adapter<WorkItemAdapter.WorkItemViewHolder>(), View.OnClickListener {

    @Inject
    lateinit var myViewModelFactory: MyViewModelFactory

    private val viTalkVM: ViTalkVM

    init {
        val mainActivity = firstFragment.activity as MainActivity
        mainActivity.getActivityComponent()
            .inject(this)

        viTalkVM = ViewModelProvider(mainActivity, myViewModelFactory)[ViTalkVM::class.java]
    }

    class WorkItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var youTubeThumbnail: ImageView = itemView.findViewById(R.id.c_youtube_thumbnail)
        var youTubeId: TextView = itemView.findViewById(R.id.c_youtube_id)
        var youTubeTitle: TextView = itemView.findViewById(R.id.c_youtube_title)
        var selectButton: MaterialButton = itemView.findViewById(R.id.c_work_button)
        var previewButton: MaterialButton = itemView.findViewById(R.id.c_preview_button)
        var shareButton: MaterialButton = itemView.findViewById(R.id.c_share_button)
        var favButton: MaterialButton = itemView.findViewById(R.id.c_fav_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkItemViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.work_item_recycler_row, parent, false)
        return WorkItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: WorkItemViewHolder, position: Int) {
        val current = videoList[position]
        current.positionInAdapter = position
        holder.previewButton.isEnabled = current.recordExists
        holder.shareButton.isEnabled = current.recordExists

        holder.previewButton.setOnClickListener {
            if (viTalkVM.noGoogleSignIn()) {
                viTalkVM.uiActionMutableLiveData.value = ViTalkVM.GOOGLE_SIGNIN_ACTION_CODE
                return@setOnClickListener
            }
            viTalkVM.youtubeVideoIdToShareOrPreview = current.youTubeId
            viTalkVM.uiActionMutableLiveData.value = ViTalkVM.PREVIEW_ACTION_CODE
        }

        holder.shareButton.setOnClickListener {
            if (viTalkVM.noGoogleSignIn()) {
                viTalkVM.uiActionMutableLiveData.value = ViTalkVM.GOOGLE_SIGNIN_ACTION_CODE
                return@setOnClickListener
            }
            viTalkVM.youtubeVideoIdToShareOrPreview = current.youTubeId
            viTalkVM.uiActionMutableLiveData.value = ViTalkVM.SHARE_ACTION_CODE
        }

        if (!viTalkVM.checkImageLoaded(current.youTubeId)) {
            holder.youTubeThumbnail.setImageResource(R.drawable.q_default)
        } else {
            holder.youTubeThumbnail.setImageBitmap(viTalkVM.lookupForBitmap(current.youTubeId))
        }

        holder.youTubeId.text = "YouTube ID: " + current.youTubeId
        holder.youTubeTitle.text = if (current.title == null) current.youTubeId else current.title
        holder.selectButton.tag = current
        holder.selectButton.setOnClickListener(this)
        holder.favButton.tag = viTalkVM.checkIfFavorite(current.youTubeId)
        holder.favButton.setIconResource(
            if (viTalkVM.checkIfFavorite(current.youTubeId)) R.drawable.ic_baseline_favorite_24
            else R.drawable.ic_baseline_favorite_border_24
        )

        holder.favButton.setOnClickListener {
            if (holder.favButton.tag as Boolean) {
                holder.favButton.setIconResource(R.drawable.ic_baseline_favorite_border_24)
                viTalkVM.processFavoriteRemoved((holder.selectButton.tag as WorkItemVideo).youTubeId)
            } else {
                holder.favButton.setIconResource(R.drawable.ic_baseline_favorite_24)
                viTalkVM.processFavoriteAdded((holder.selectButton.tag as WorkItemVideo).youTubeId)
            }
            holder.favButton.tag = !(holder.favButton.tag as Boolean)
        }
    }

    override fun onClick(v: View) {
        if (viTalkVM.noGoogleSignIn()) {
            viTalkVM.uiActionMutableLiveData.value = ViTalkVM.GOOGLE_SIGNIN_ACTION_CODE
            return
        }
        val current = v.tag as WorkItemVideo
        if (current.recordExists) {
            val dialog = AlertDialog.Builder(
                firstFragment.requireContext()
            ).create()
            dialog.setTitle("Record exist")
            dialog.setMessage("Are you sure you want to overwrite?")
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES") { dialogInterface, _ ->
                firstFragment.setVideoIdForWorkNavigateToSecond(current.youTubeId)
                dialogInterface.cancel()
            }
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "NO"
            ) { dialogInterface, _ -> dialogInterface.cancel() }
            dialog.setOnDismissListener { dialogInterface -> dialogInterface.cancel() }
            dialog.show()
        } else {
            firstFragment.setVideoIdForWorkNavigateToSecond(current.youTubeId)
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    fun deleteItem(position: Int) {
        val idToRemove = videoList[position].youTubeId
        videoList.removeAt(position)
        notifyItemRemoved(position)
        viTalkVM.onWorkItemDeleted(idToRemove)
    }

    val context: Context?
        get() = firstFragment.context
}