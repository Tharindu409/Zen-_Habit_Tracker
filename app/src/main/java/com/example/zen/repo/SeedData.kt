package com.example.zen.repo

import com.example.zen.models.Habit

object SeedData {
    fun builtInHabits(): List<Habit> {
        return listOf(
            Habit(
                id = "habit_water",
                title = "Drink Water",
                emoji = "\uD83D\uDCA7", // 💧
                unit = "ML",
                targetPerDay = 2000,    // 2000 mL
                defaultIncrement = 250, // 250 mL
                category = "Health",
                isBuiltIn = true,
                isStarred = true,
                reminderTimes = listOf("09:00", "12:00", "15:00", "18:00")
            ),
            Habit(
                id = "habit_meditate",
                title = "Meditate",
                emoji = "\uD83E\uDDD8", // 🧘
                unit = "MINUTES",
                targetPerDay = 10,
                defaultIncrement = 5,
                category = "Mindfulness",
                isBuiltIn = true,
                isStarred = true,
                reminderTimes = listOf("20:00")
            ),
            Habit(
                id = "habit_steps",
                title = "Steps",
                emoji = "\uD83D\uDC63", // 👣
                unit = "STEPS",
                targetPerDay = 6000,
                defaultIncrement = 1000,
                category = "Health",
                isBuiltIn = true,
                isStarred = true,
                reminderTimes = emptyList()
            )
        )
    }
}