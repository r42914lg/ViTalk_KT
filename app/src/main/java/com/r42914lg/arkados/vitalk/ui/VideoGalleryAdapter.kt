package com.r42914lg.arkados.vitalk.ui

import com.r42914lg.arkados.vitalk.R
import android.view.LayoutInflater
import android.view.ViewGroup
import com.r42914lg.arkados.vitalk.model.VideoGalleryScanner
import com.r42914lg.arkados.vitalk.model.LocalVideo
import android.content.DialogInterface
import com.r42914lg.arkados.vitalk.ui.VideoGalleryAdapter.VideoGalleryViewHolder
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class VideoGalleryAdapter(
    private val videoGalleryScanner: VideoGalleryScanner,
    private val thirdFragment: ThirdFragment
) : RecyclerView.Adapter<VideoGalleryViewHolder>() {

    class VideoGalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView= itemView.findViewById(R.id.video_thumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoGalleryViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_recycler_element, parent, false)

        return VideoGalleryViewHolder(v)
    }

    override fun onBindViewHolder(holder: VideoGalleryViewHolder, position: Int) {
        val localVideo = videoGalleryScanner.videoList[position]
        val thumbNailBmp = videoGalleryScanner.loadThumbnail(localVideo!!.uri)

        holder.videoThumbnail.setImageBitmap(thumbNailBmp)
        holder.videoThumbnail.tag = localVideo
        holder.videoThumbnail.setOnClickListener { view: View ->
            val dialog = thirdFragment.context?.let { AlertDialog.Builder(it).create() }

            dialog?.setTitle(thirdFragment.context!!.getString(R.string.dialog_youtube_upload_title))
            dialog?.setMessage(thirdFragment.context!!.getString(R.string.dialog_youtube_upload_text))
            dialog?.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "OK"
            ) { dialog1: DialogInterface, _: Int ->
                val video = view.tag as LocalVideo
                thirdFragment.startVideoUploadNavigateToFirst(video)
                dialog1.cancel()
            }
            dialog?.setOnDismissListener { obj: DialogInterface -> obj.cancel() }
            dialog?.show()
        }
    }

    override fun getItemCount(): Int {
        return videoGalleryScanner.videoList.size
    }
}