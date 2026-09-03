package com.example.orgamam.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val groupId: String = "",
    val hexColor: String = "#6200EE"
)
