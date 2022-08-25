package com.r42914lg.arkados.vitalk.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageHelper @Inject constructor(
    private val dataLoaderListener: IDataLoaderListener,
    private val cs:  CoroutineScope,
    app: Application) {

    private val preferences: SharedPreferences = app.getSharedPreferences(
        "sharedPrefs",
        Context.MODE_PRIVATE
    )

    fun cancelCS() {
        cs.cancel()
    }

    fun loadWorkItems(): MutableList<WorkItemVideo> {
        val workItemVideoList: MutableList<WorkItemVideo> = CopyOnWriteArrayList()

        val youTubeIDs = preferences.getStringSet("YOUTUBE_IDs", HashSet())
        if (youTubeIDs != null) {
            for (youTubeId in youTubeIDs) {
                workItemVideoList.add(
                    WorkItemVideo(
                        preferences.getBoolean(youTubeId + "_HAS_RECORD", false),
                        youTubeId
                    )
                )
            }
        }

        return workItemVideoList
    }

    fun storeWorkItems(workItemVideoList: List<WorkItemVideo>) {

        val editor = preferences.edit()
        val youTubeIDs: MutableSet<String> = HashSet()

        for (workItemVideo in workItemVideoList) {
            youTubeIDs.add(workItemVideo.youTubeId)
            editor.remove(workItemVideo.youTubeId + "_HAS_RECORD")
        }

        editor.remove("YOUTUBE_IDs")
        editor.putStringSet("YOUTUBE_IDs", youTubeIDs)

        for (workItemVideo in workItemVideoList) {
            editor.putBoolean(workItemVideo.youTubeId + "_HAS_RECORD", workItemVideo.recordExists)
        }

        editor.apply()
    }

    fun loadFavorites(): MutableSet<String> {
        val result = mutableSetOf<String>()

        val temp = preferences.getStringSet("FAVORITE_IDs", HashSet())
        temp?.apply {
            for (s in this) {
                result.add(s)
            }
        }

        return result
    }

    fun storeFavorites(favorites: Set<String?>?) {
        val editor = preferences.edit()
        editor.remove("FAVORITE_IDs")
        editor.putStringSet("FAVORITE_IDs", favorites)
        editor.apply()
    }

    fun loadImageFromURL(youTubeId: String) {
        cs.launch {
            var bitmap: Bitmap
            try {
                withContext(Dispatchers.IO) {
                    bitmap = BitmapFactory.decodeStream(
                        URL("https://img.youtube.com/vi/$youTubeId/0.jpg")
                            .openConnection().getInputStream()
                    )
                }
                dataLoaderListener.callbackLoadImageFromURL(bitmap, youTubeId)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun queryYouTubeTitleFromURL(youTubeId: String) {
        val stringUrl =
            "https://www.youtube.com/oembed?url=youtube.com/watch?v=$youTubeId&format=json"

        cs.launch {
            var title: String? = null

            withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                var reader: BufferedReader? = null

                try {

                    val url = URL(stringUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()

                    reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val buffer = StringBuilder()

                    var line = reader.readLine()
                    while (line != null) {
                        buffer.append(line).append("\n")
                        line = reader.readLine()
                    }

                    val result = buffer.toString()
                    val jsonResponse = JSONObject(result)

                    title = jsonResponse.getString("title")

                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                    try {
                        reader?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            title?.apply {
                dataLoaderListener.callbackVideoTileReceived(this, youTubeId)
            }
        }
    }
}