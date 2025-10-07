package com.example.zen.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.zen.repo.ZenRepository
import com.example.zen.util.DateUtils

class ZenWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PLUS = "com.example.zen.widget.ACTION_PLUS"
        const val EXTRA_HABIT_ID = "extra_widget_habit_id"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        android.util.Log.d("ZenWidget", "onReceive called with action: $action")
        val repo = ZenRepository.getInstance(context)
        if (action == ACTION_WIDGET_PLUS) {
            val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
            android.util.Log.d("ZenWidget", "Plus button clicked for habit ID: $habitId")
            if (habitId.isNullOrEmpty()) {
                android.util.Log.e("ZenWidget", "Habit ID is null or empty!")
                return
            }
            repo.getHabit(habitId)?.let { habit ->
                android.util.Log.d("ZenWidget", "Adding tick for habit: ${habit.title}, increment: ${habit.defaultIncrement}")
                repo.addTick(habitId, habit.defaultIncrement)
                WidgetUpdateHelper.updateAllWidgets(context)
                android.util.Log.d("ZenWidget", "Widget updated successfully")
            } ?: android.util.Log.e("ZenWidget", "Habit not found for ID: $habitId")
        }
    }

    object WidgetUpdateHelper {
        fun updateAllWidgets(context: Context) {
            val widgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, ZenWidgetProvider::class.java.name)
            val appWidgetIds = widgetManager.getAppWidgetIds(thisAppWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, widgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(context: Context, widgetManager: AppWidgetManager, appWidgetId: Int) {
            val repo = ZenRepository.getInstance(context)
            val settings = repo.getSettings()
            val rv = RemoteViews(context.packageName, com.example.zen.R.layout.widget_pulse)
            
            // Calculate overall completion percentage and habit count
            val habits = repo.getAllHabits()
            val today = DateUtils.nowDateString()
            val ticks = repo.getTicksForDate(today)
            
            var totalCompletion = 0.0
            var completedHabits = 0
            var totalHabits = habits.size
            
            habits.forEach { habit ->
                val done = ticks.firstOrNull { it.habitId == habit.id }?.amount ?: 0
                val target = habit.targetPerDay
                val completion = if (target > 0) {
                    (done.toDouble() / target.toDouble()).coerceAtMost(1.0)
                } else 0.0
                totalCompletion += completion
                if (completion >= 1.0) completedHabits++
            }
            
            val overallPercent = if (totalHabits > 0) {
                ((totalCompletion / totalHabits) * 100).toInt()
            } else 0
            
            // Set cheer message based on completion
            val cheerMessage = getCheerMessage(overallPercent, completedHabits, totalHabits)
            rv.setTextViewText(com.example.zen.R.id.tvWidgetCheerMessage, cheerMessage)
            
            // Set progress count and bar
            rv.setTextViewText(com.example.zen.R.id.tvWidgetProgressCount, "$completedHabits/$totalHabits")
            rv.setProgressBar(com.example.zen.R.id.progressBarWidget, 100, overallPercent, false)
            
            // Handle selected habit
            val selectedHabitId = settings.widgetSelectedHabitId
            if (selectedHabitId.isNotEmpty()) {
                val selectedHabit = repo.getHabit(selectedHabitId)
                if (selectedHabit != null) {
                    val done = ticks.firstOrNull { it.habitId == selectedHabitId }?.amount ?: 0
                    val target = selectedHabit.targetPerDay
                    val habitPercent = if (target > 0) {
                        ((done.toDouble() / target.toDouble()) * 100).toInt().coerceAtMost(100)
                    } else 0
                    
                    rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitEmoji, selectedHabit.emoji)
                    rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitTitle, selectedHabit.title)
                    
                    // Format progress text based on unit
                    val progressText = when (selectedHabit.unit) {
                        "ML" -> "$done / $target mL"
                        "LITERS" -> "$done / $target L"
                        "MINUTES" -> "$done / $target min"
                        "STEPS" -> "$done / $target steps"
                        else -> "$done / $target"
                    }
                    rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitProgress, progressText)
                    rv.setProgressBar(com.example.zen.R.id.progressBarHabit, 100, habitPercent, false)
                    rv.setViewVisibility(com.example.zen.R.id.layoutHabitControls, View.VISIBLE)
                    rv.setViewVisibility(com.example.zen.R.id.layoutSelectedHabitCard, View.VISIBLE)
                    
                    // Set up click listeners for plus buttons
                    // Use unique request codes based on widget ID and habit ID hashcode to avoid conflicts
                    val plusIntent = Intent(context, ZenWidgetProvider::class.java).apply {
                        action = ACTION_WIDGET_PLUS
                        putExtra(EXTRA_HABIT_ID, selectedHabitId)
                    }
                    val requestCode = (appWidgetId.toString() + selectedHabitId).hashCode()
                    val plusPendingIntent = PendingIntent.getBroadcast(
                        context, 
                        requestCode, 
                        plusIntent, 
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    rv.setOnClickPendingIntent(com.example.zen.R.id.btnWidgetPlus, plusPendingIntent)
                    rv.setOnClickPendingIntent(com.example.zen.R.id.btnWidgetQuickAdd, plusPendingIntent)
                } else {
                    // Selected habit not found
                    setupNoHabitSelected(rv)
                }
            } else {
                // No habit selected
                setupNoHabitSelected(rv)
            }
            
            widgetManager.updateAppWidget(appWidgetId, rv)
        }
        
        private fun setupNoHabitSelected(rv: RemoteViews) {
            rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitEmoji, "📊")
            rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitTitle, "No habit selected")
            rv.setTextViewText(com.example.zen.R.id.tvWidgetHabitProgress, "Choose a habit in settings")
            rv.setProgressBar(com.example.zen.R.id.progressBarHabit, 100, 0, false)
            rv.setViewVisibility(com.example.zen.R.id.layoutHabitControls, View.GONE)
            rv.setViewVisibility(com.example.zen.R.id.layoutSelectedHabitCard, View.VISIBLE)
        }
        
        private fun getCheerMessage(percent: Int, completed: Int, total: Int): String {
            return when {
                percent == 100 -> "🎉 Perfect day!"
                percent >= 80 -> "🔥 Almost there!"
                percent >= 60 -> "💪 Great progress!"
                percent >= 40 -> "👍 Keep it up!"
                percent >= 20 -> "🌱 Good start!"
                completed > 0 -> "✨ You got this!"
                else -> "🚀 Ready to begin?"
            }
        }
    }
}