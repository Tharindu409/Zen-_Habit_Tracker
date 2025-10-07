package com.example.zen.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.zen.models.MoodEntry
import kotlin.math.min

/**
 * Modern mood statistics visualization view with animated charts and insights
 */
class MoodStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintPrimary = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintSecondary = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var moodData: List<MoodEntry> = emptyList()
    private var animationProgress = 0f
    private var animator: ValueAnimator? = null
    
    // Statistics
    private var averageMood = 0f
    private var moodDistribution = IntArray(5) { 0 }
    private var currentStreak = 0
    private var longestStreak = 0
    
    private val padding = 48f
    private val barWidth = 40f
    private val barSpacing = 24f
    
    init {
        setupPaints()
    }
    
    private fun setupPaints() {
        paintText.apply {
            textSize = 36f
            color = 0xFF1E1E1E.toInt()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        paintGlow.apply {
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
        }
    }
    
    fun setMoodData(data: List<MoodEntry>) {
        moodData = data
        calculateStatistics()
        animateIn()
    }
    
    private fun calculateStatistics() {
        if (moodData.isEmpty()) {
            averageMood = 0f
            moodDistribution = IntArray(5) { 0 }
            currentStreak = 0
            longestStreak = 0
            return
        }
        
        // Calculate average mood
        averageMood = moodData.map { it.score }.average().toFloat()
        
        // Calculate mood distribution
        moodDistribution = IntArray(5) { 0 }
        moodData.forEach { mood ->
            if (mood.score in 1..5) {
                moodDistribution[mood.score - 1]++
            }
        }
        
        // Calculate streaks (consecutive days with mood entries)
        calculateStreaks()
    }
    
    private fun calculateStreaks() {
        if (moodData.isEmpty()) {
            currentStreak = 0
            longestStreak = 0
            return
        }
        
        val sortedByDate = moodData.sortedByDescending { it.timestamp }
        var streak = 1
        var maxStreak = 1
        
        // Check current streak
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        for (i in 0 until sortedByDate.size - 1) {
            val current = java.util.Calendar.getInstance()
            current.timeInMillis = sortedByDate[i].timestamp
            current.set(java.util.Calendar.HOUR_OF_DAY, 0)
            current.set(java.util.Calendar.MINUTE, 0)
            current.set(java.util.Calendar.SECOND, 0)
            current.set(java.util.Calendar.MILLISECOND, 0)
            
            val next = java.util.Calendar.getInstance()
            next.timeInMillis = sortedByDate[i + 1].timestamp
            next.set(java.util.Calendar.HOUR_OF_DAY, 0)
            next.set(java.util.Calendar.MINUTE, 0)
            next.set(java.util.Calendar.SECOND, 0)
            next.set(java.util.Calendar.MILLISECOND, 0)
            
            val dayDiff = ((current.timeInMillis - next.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            
            if (dayDiff == 1) {
                streak++
                maxStreak = maxOf(maxStreak, streak)
            } else {
                streak = 1
            }
        }
        
        currentStreak = if (sortedByDate.isNotEmpty()) {
            val mostRecent = java.util.Calendar.getInstance()
            mostRecent.timeInMillis = sortedByDate[0].timestamp
            mostRecent.set(java.util.Calendar.HOUR_OF_DAY, 0)
            mostRecent.set(java.util.Calendar.MINUTE, 0)
            mostRecent.set(java.util.Calendar.SECOND, 0)
            mostRecent.set(java.util.Calendar.MILLISECOND, 0)
            
            val today = java.util.Calendar.getInstance()
            today.set(java.util.Calendar.HOUR_OF_DAY, 0)
            today.set(java.util.Calendar.MINUTE, 0)
            today.set(java.util.Calendar.SECOND, 0)
            today.set(java.util.Calendar.MILLISECOND, 0)
            
            val daysSinceLastEntry = ((today.timeInMillis - mostRecent.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysSinceLastEntry <= 1) streak else 0
        } else {
            0
        }
        
        longestStreak = maxOf(maxStreak, currentStreak)
    }
    
    private fun animateIn() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (moodData.isEmpty()) {
            drawEmptyState(canvas)
            return
        }
        
        val centerY = height / 2f
        val startX = padding
        
        // Draw mood distribution bars
        drawMoodDistribution(canvas, startX, centerY)
        
        // Draw statistics text
        drawStatistics(canvas, startX)
    }
    
    private fun drawMoodDistribution(canvas: Canvas, startX: Float, centerY: Float) {
        val maxCount = moodDistribution.maxOrNull() ?: 1
        val maxBarHeight = (height - 2 * padding) * 0.4f
        
        val emojis = arrayOf("😢", "😕", "😐", "😊", "😁")
        val colors = intArrayOf(
            0xFFFF5722.toInt(), // Red
            0xFFFF9800.toInt(), // Orange
            0xFFFFC107.toInt(), // Yellow
            0xFF8BC34A.toInt(), // Light Green
            0xFF4CAF50.toInt()  // Green
        )
        
        moodDistribution.forEachIndexed { index, count ->
            val x = startX + index * (barWidth + barSpacing)
            val barHeight = if (maxCount > 0) {
                (count.toFloat() / maxCount) * maxBarHeight * animationProgress
            } else {
                0f
            }
            val y = centerY + maxBarHeight - barHeight
            
            // Draw glow effect
            paintGlow.color = colors[index] and 0x40FFFFFF.toInt()
            canvas.drawRoundRect(
                x - 4, y - 4,
                x + barWidth + 4, centerY + maxBarHeight + 4,
                12f, 12f, paintGlow
            )
            
            // Draw bar with gradient
            val gradient = LinearGradient(
                x, y,
                x, centerY + maxBarHeight,
                colors[index],
                colors[index] and 0x80FFFFFF.toInt(),
                Shader.TileMode.CLAMP
            )
            paintPrimary.shader = gradient
            paintPrimary.style = Paint.Style.FILL
            
            val rect = RectF(x, y, x + barWidth, centerY + maxBarHeight)
            canvas.drawRoundRect(rect, 12f, 12f, paintPrimary)
            paintPrimary.shader = null
            
            // Draw emoji
            paintText.textSize = 32f
            canvas.drawText(
                emojis[index],
                x + barWidth / 2,
                centerY + maxBarHeight + 48f,
                paintText
            )
            
            // Draw count
            if (count > 0) {
                paintText.textSize = 24f
                paintText.color = 0xFFFFFFFF.toInt()
                canvas.drawText(
                    count.toString(),
                    x + barWidth / 2,
                    y - 12f,
                    paintText
                )
                paintText.color = 0xFF1E1E1E.toInt()
            }
        }
    }
    
    private fun drawStatistics(canvas: Canvas, startX: Float) {
        val statsX = width - padding
        val statsY = padding + 40f
        
        paintText.textAlign = Paint.Align.RIGHT
        paintText.textSize = 42f
        paintText.color = 0xFF1A4C8B.toInt()
        
        // Average mood
        val avgText = String.format("%.1f", averageMood * animationProgress)
        canvas.drawText(avgText, statsX, statsY, paintText)
        
        paintText.textSize = 20f
        paintText.color = 0xFF6B7280.toInt()
        canvas.drawText("Avg Mood", statsX, statsY + 30f, paintText)
        
        // Streak
        paintText.textSize = 42f
        paintText.color = 0xFF4CAF50.toInt()
        val streakText = (currentStreak * animationProgress).toInt().toString()
        canvas.drawText(streakText, statsX, statsY + 100f, paintText)
        
        paintText.textSize = 20f
        paintText.color = 0xFF6B7280.toInt()
        canvas.drawText("Day Streak 🔥", statsX, statsY + 130f, paintText)
        
        paintText.textAlign = Paint.Align.CENTER
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        paintText.textSize = 28f
        paintText.color = 0xFF999999.toInt()
        paintText.textAlign = Paint.Align.CENTER
        
        canvas.drawText(
            "No mood data yet",
            width / 2f,
            height / 2f,
            paintText
        )
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 600
        val desiredHeight = 320
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }
        
        setMeasuredDimension(width, height)
    }
}
