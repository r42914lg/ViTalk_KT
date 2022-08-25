package com.r42914lg.arkados.vitalk.model

import android.app.Activity
import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import java.util.concurrent.TimeUnit

class VideoGalleryScanner(private val activity: Activity) {
    var videoList = mutableListOf<LocalVideo>()

    init {
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val selection = MediaStore.Video.Media.DURATION + " <= ?"
        val selectionArgs = arrayOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString())
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " ASC"

        activity.applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ).use { cursor ->
            // Cache column indices.
            cursor?.apply {

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList.add(LocalVideo(contentUri, name, duration, size))
                }
            }
        }
    }

    fun loadThumbnail(contentUri: Uri): Bitmap {
        return activity.applicationContext.contentResolver.loadThumbnail(
            contentUri,
            Size(640, 480),
            null
        )
    }
}