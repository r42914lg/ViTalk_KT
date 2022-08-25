package com.r42914lg.arkados.vitalk.model

import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.auth.FirebaseAuth
import android.net.Uri
import java.io.File

@Singleton
class FirebaseHelper @Inject constructor(private val dataLoaderListener: IDataLoaderListener) {

    init {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInAnonymously()
            .addOnCompleteListener { dataLoaderListener.callbackFirebaseAuthenticated() }
    }

    fun uploadAudioWithId(youTubeId: String, googleAccId: String, fullPath: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val islandRef = storageRef.child(googleAccId + youTubeId)
        val file = Uri.fromFile(File(fullPath))
        val metadata = StorageMetadata.Builder()
            .setContentType("audio/mpeg")
            .build()

        val uploadTask = islandRef.putFile(file, metadata)

        uploadTask.addOnFailureListener {
            dataLoaderListener.onFirebaseUploadFailed(fullPath)
        }.addOnSuccessListener {
            dataLoaderListener.onFirebaseUploadFinished(youTubeId)
        }
    }
}