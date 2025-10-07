package com.example.zen.models

import kotlinx.serialization.Serializable

@Serializable
data class HabitTick constructor(
    val habitId: String,
    val date: String,   // "yyyy-MM-dd"
    val amount: Int     // base units
)