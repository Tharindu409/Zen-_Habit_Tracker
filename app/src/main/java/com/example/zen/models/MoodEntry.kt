package com.example.zen.models

import kotlinx.serialization.Serializable

@Serializable
data class MoodEntry constructor(
    val id: String,
    val emoji: String,
    val note: String? = null,
    val timestamp: Long,
    val score: Int
)