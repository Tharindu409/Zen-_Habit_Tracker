package com.example.zen.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zen.widgets.ZenWidgetProvider
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class MidnightWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ZenWidgetProvider.WidgetUpdateHelper.updateAllWidgets(applicationContext)
        return Result.success()
    }
}

// Helper function to calculate delay to next midnight
fun calculateDelayToNextMidnight(): Long {
    val now = LocalTime.now(ZoneId.systemDefault())
    val midnight = LocalTime.MIDNIGHT
    val today = LocalDate.now(ZoneId.systemDefault())
    val nextMidnight = if (now.isBefore(midnight)) {
        today.atTime(midnight)
    } else {
        today.plusDays(1).atTime(midnight)
    }
    return ChronoUnit.MILLIS.between(
        today.atTime(now),
        nextMidnight
    )
}