package com.example.orgamam.service

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class UpdateInfo(
    val latestVersionCode: Int = 1,
    val apkUrl: String = "",
    val whatsNew: String = ""
)

class UpdateManager {
    private val db = Firebase.firestore
    
    private val _updateState = MutableStateFlow<UpdateInfo?>(null)
    val updateState: StateFlow<UpdateInfo?> = _updateState.asStateFlow()

    suspend fun checkUpdates() {
        try {
            val doc = db.collection("config").document("update").get()
            val info = doc.data<UpdateInfo>()
            if (info.latestVersionCode > getVersionCode()) {
                _updateState.value = info
            }
        } catch (e: Exception) {
            // Ignore for now
        }
    }
}
