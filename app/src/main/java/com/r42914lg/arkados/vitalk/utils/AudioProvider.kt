package com.r42914lg.arkados.vitalk.utils

import android.content.ContentProvider
import android.os.ParcelFileDescriptor
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.r42914lg.arkados.vitalk.ViTalkConstants
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import java.net.URLConnection

class AudioProvider : ContentProvider() {
    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://com.r42914lg.arkados.vitalk_result")
    }

    override fun onCreate(): Boolean {
        context?.apply {
            val f = File(this.externalCacheDir, ViTalkConstants.FILE_NAME)
            return f.exists()
        }
        return false
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        lateinit var f: File
        uri.path?.apply { f = File(context!!.externalCacheDir, this) }
        if (!f.exists())
            throw FileNotFoundException()

        var afd: ParcelFileDescriptor? = null
        try {
            afd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.parseMode(mode))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return afd
    }

    override fun getType(uri: Uri): String? {
        return URLConnection.guessContentTypeFromName(uri.toString())
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        throw RuntimeException("Operation not supported")
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        throw RuntimeException("Operation not supported")
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        throw RuntimeException("Operation not supported")
    }

    override fun update(
        uri: Uri,
        contentValues: ContentValues?,
        s: String?,
        strings: Array<String>?
    ): Int {
        throw RuntimeException("Operation not supported")
    }
}