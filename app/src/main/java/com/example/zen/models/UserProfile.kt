package com.example.zen.models

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile constructor(
    val name: String,
    val age: Int,
    val gender: String,        // "Male","Female","Other","PreferNotToSay"
    val timezoneId: String,
    val avatarEmoji: String = "\uD83E\uDEA3", // default 🫣 or similar
    val createdAt: Long = System.currentTimeMillis()
)