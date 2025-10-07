package com.example.zen.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.zen.models.MoodEntry
import com.example.zen.util.EmojiPalette
import com.example.zen.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MoodChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var moodData: List<MoodEntry> = emptyList()
    private var timeframe: Timeframe = Timeframe.TODAY
    private var chartPoints: List<ChartPoint> = emptyList()
    
    private var animationProgress = 0f
    private var animator: ValueAnimator? = null
    
    private val padding = 70f
    private val pointRadius = 8f
    private val textSize = 36f
    private val gridTextSize = 30f
    
    enum class Timeframe {
        TODAY, DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    data class ChartPoint(
        val x: Float,
        val y: Float,
        val value: Int,
        val timestamp: Long,
        val emoji: String
    )
    
    init {
        setupPaints()
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Enable shadow rendering
        // Ensure we recalculate once the view is laid out
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (width > 0 && height > 0 && moodData.isNotEmpty()) {
                calculateChartPoints()
                animateIn()
            }
        }
    }
    
    private fun setupPaints() {
        // Line paint for connecting mood points - Modern gradient stroke
        linePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            setShadowLayer(8f, 0f, 4f, 0x40000000)
        }
        
        // Point paint for mood dots
        pointPaint.apply {
            style = Paint.Style.FILL
            setShadowLayer(6f, 0f, 3f, 0x60000000)
        }
        
        // Shadow paint for glowing effects
        shadowPaint.apply {
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
        }
        
        // Fill paint for area under curve
        fillPaint.apply {
            style = Paint.Style.FILL
        }
        
        // Text paint for labels
        textPaint.apply {
            color = 0xFF666666.toInt()
            textSize = this@MoodChartView.textSize
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        // Grid paint for axis lines - Subtle modern lines
        gridPaint.apply {
            color = 0xFFE8E8E8.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        
        // Background gradient paint
        backgroundPaint.apply {
            style = Paint.Style.FILL
        }
    }
    
    private fun animateIn() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    fun setMoodData(data: List<MoodEntry>) {
        moodData = data
        if (width > 0 && height > 0) {
            calculateChartPoints()
            animateIn()
        } else {
            // Defer calculation until layout is ready
            requestLayout()
        }
    }
    
    fun setTimeframe(timeframe: Timeframe) {
        this.timeframe = timeframe
        if (width > 0 && height > 0) {
            calculateChartPoints()
            animateIn()
        } else {
            requestLayout()
        }
    }
    
    private fun calculateChartPoints() {
        if (moodData.isEmpty()) {
            chartPoints = emptyList()
            return
        }
        // If view size isn't ready yet, defer calculation
        if (width <= 0 || height <= 0) {
            post {
                if (width > 0 && height > 0) {
                    calculateChartPoints()
                    invalidate()
                }
            }
            return
        }
        
        val filteredData = when (timeframe) {
            Timeframe.TODAY -> getTodayMoods()
            Timeframe.DAILY -> getLastNDays(7)
            Timeframe.WEEKLY -> getLastNWeeks(8)
            Timeframe.MONTHLY -> getLastNMonths(12)
            Timeframe.YEARLY -> getLastNYears(5)
        }
        
        if (filteredData.isEmpty()) {
            chartPoints = emptyList()
            return
        }
        
        val points = mutableListOf<ChartPoint>()
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        filteredData.forEachIndexed { index, mood ->
            val x = if (filteredData.size == 1) {
                width / 2f // Center single point
            } else {
                padding + (index * chartWidth / (filteredData.size - 1))
            }
            val normalizedValue = (mood.score - 1) / 4f // Normalize 1-5 to 0-1
            val y = padding + chartHeight - (normalizedValue * chartHeight)
            
            points.add(ChartPoint(
                x = x,
                y = y,
                value = mood.score,
                timestamp = mood.timestamp,
                emoji = mood.emoji
            ))
        }
        
        chartPoints = points
    }
    
    private fun getTodayMoods(): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        return moodData
            .filter { it.timestamp >= startTime && it.timestamp <= endTime }
            .sortedBy { it.timestamp }
    }
    
    private fun getLastNDays(days: Int): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startTime = calendar.timeInMillis
        
        return moodData
            .filter { it.timestamp >= startTime && it.timestamp <= endTime }
            .sortedBy { it.timestamp }
    }
    
    private fun getLastNWeeks(weeks: Int): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.WEEK_OF_YEAR, -weeks)
        val startTime = calendar.timeInMillis
        
        // Group by week and average the scores
        return moodData
            .filter { it.timestamp >= startTime && it.timestamp <= endTime }
            .groupBy { getWeekOfYear(it.timestamp) }
            .map { (_, moods) ->
                val avgScore = moods.map { it.score }.average().toInt().coerceIn(1, 5)
                val representativeEmoji = EmojiPalette.EMOJIS.find { it.score == avgScore }?.emoji ?: "😐"
                moods.first().copy(
                    score = avgScore,
                    emoji = representativeEmoji,
                    timestamp = moods.map { it.timestamp }.average().toLong()
                )
            }
            .sortedBy { it.timestamp }
    }
    
    private fun getLastNMonths(months: Int): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -months)
        val startTime = calendar.timeInMillis
        
        // Group by month and average the scores
        return moodData
            .filter { it.timestamp >= startTime && it.timestamp <= endTime }
            .groupBy { getMonthOfYear(it.timestamp) }
            .map { (_, moods) ->
                val avgScore = moods.map { it.score }.average().toInt().coerceIn(1, 5)
                val representativeEmoji = EmojiPalette.EMOJIS.find { it.score == avgScore }?.emoji ?: "😐"
                moods.first().copy(
                    score = avgScore,
                    emoji = representativeEmoji,
                    timestamp = moods.map { it.timestamp }.average().toLong()
                )
            }
            .sortedBy { it.timestamp }
    }
    
    private fun getLastNYears(years: Int): List<MoodEntry> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -years)
        val startTime = calendar.timeInMillis
        
        // Group by year and average the scores
        return moodData
            .filter { it.timestamp >= startTime && it.timestamp <= endTime }
            .groupBy { getYear(it.timestamp) }
            .map { (_, moods) ->
                val avgScore = moods.map { it.score }.average().toInt().coerceIn(1, 5)
                val representativeEmoji = EmojiPalette.EMOJIS.find { it.score == avgScore }?.emoji ?: "😐"
                moods.first().copy(
                    score = avgScore,
                    emoji = representativeEmoji,
                    timestamp = moods.map { it.timestamp }.average().toLong()
                )
            }
            .sortedBy { it.timestamp }
    }
    
    private fun getWeekOfYear(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }
    
    private fun getMonthOfYear(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
    }
    
    private fun getYear(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR).toString()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width == 0 || height == 0) return
        
        drawBackground(canvas)
        drawGrid(canvas)
        
        if (chartPoints.isEmpty()) {
            drawEmptyState(canvas)
        } else {
            drawMoodLine(canvas)
            drawMoodPoints(canvas)
            drawLabels(canvas)
        }
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        textPaint.textSize = textSize
        textPaint.color = 0xFF999999.toInt()
        textPaint.textAlign = Paint.Align.CENTER
        
        val message = when (timeframe) {
            Timeframe.TODAY -> "No moods logged today"
            Timeframe.DAILY -> "No moods in the past week"
            Timeframe.WEEKLY -> "No moods in the past 8 weeks"
            Timeframe.MONTHLY -> "No moods in the past year"
            Timeframe.YEARLY -> "No moods in the past 5 years"
        }
        
        canvas.drawText(
            message,
            width / 2f,
            height / 2f,
            textPaint
        )
    }
    
    private fun drawBackground(canvas: Canvas) {
        // Modern gradient background with smooth color transitions
        val chartHeight = height - 2 * padding
        val colors = intArrayOf(
            0x15FF5722, // Subtle red zone (bottom - sad moods)
            0x15FFC107, // Subtle yellow zone (middle - neutral)
            0x154CAF50  // Subtle green zone (top - happy moods)
        )
        val positions = floatArrayOf(0f, 0.5f, 1f)
        
        val gradient = LinearGradient(
            0f, padding + chartHeight,
            0f, padding,
            colors, positions,
            Shader.TileMode.CLAMP
        )
        
        backgroundPaint.shader = gradient
        val rect = RectF(padding, padding, width - padding, height - padding)
        canvas.drawRoundRect(rect, 16f, 16f, backgroundPaint)
        backgroundPaint.shader = null
    }
    
    private fun drawGrid(canvas: Canvas) {
        val chartHeight = height - 2 * padding
        
        // Draw horizontal grid lines for mood levels
        for (i in 1..5) {
            val y = padding + chartHeight - ((i - 1) * chartHeight / 4)
            canvas.drawLine(padding + 40, y, width - padding, y, gridPaint)
            
            // Draw mood level labels with emojis
            val label = when (i) {
                1 -> "😢"
                2 -> "😕"
                3 -> "😐"
                4 -> "😊"
                5 -> "😁"
                else -> ""
            }
            
            textPaint.textSize = gridTextSize
            textPaint.color = 0xFF999999.toInt()
            canvas.drawText(label, padding - 10, y + gridTextSize / 3, textPaint)
        }
    }
    
    private fun drawMoodLine(canvas: Canvas) {
        if (chartPoints.size < 2) return
        
        // Animate the line drawing
        val animatedPoints = chartPoints.take((chartPoints.size * animationProgress).toInt().coerceAtLeast(1))
        if (animatedPoints.size < 2) return
        
        // Create smooth curved path
        val path = Path()
        val fillPath = Path()
        
        animatedPoints.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
                fillPath.moveTo(point.x, height - padding)
                fillPath.lineTo(point.x, point.y)
            } else {
                val prevPoint = animatedPoints[index - 1]
                val controlPointX = (prevPoint.x + point.x) / 2f
                
                // Use quadratic curve for smoother lines
                path.quadTo(controlPointX, prevPoint.y, point.x, point.y)
                fillPath.quadTo(controlPointX, prevPoint.y, point.x, point.y)
            }
        }
        
        // Complete the fill path
        val lastPoint = animatedPoints.last()
        fillPath.lineTo(lastPoint.x, height - padding)
        fillPath.close()
        
        // Draw fill area with gradient
        val fillGradient = LinearGradient(
            0f, padding,
            0f, height - padding,
            intArrayOf(0x404CAF50, 0x104CAF50),
            null,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = fillGradient
        canvas.drawPath(fillPath, fillPaint)
        fillPaint.shader = null
        
        // Draw the line with vibrant gradient
        val lineGradient = LinearGradient(
            0f, padding,
            0f, height - padding,
            intArrayOf(0xFF4CAF50.toInt(), 0xFF81C784.toInt(), 0xFFFFB74D.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        linePaint.shader = lineGradient
        canvas.drawPath(path, linePaint)
        linePaint.shader = null
    }
    
    private fun drawMoodPoints(canvas: Canvas) {
        val animatedPoints = chartPoints.take((chartPoints.size * animationProgress).toInt().coerceAtLeast(1))
        
        animatedPoints.forEach { point ->
            // Set color based on mood value with vibrant colors
            val moodColor = when (point.value) {
                1 -> 0xFFFF5722.toInt() // Vibrant Red
                2 -> 0xFFFF9800.toInt() // Vibrant Orange
                3 -> 0xFFFFC107.toInt() // Vibrant Yellow
                4 -> 0xFF8BC34A.toInt() // Vibrant Light Green
                5 -> 0xFF4CAF50.toInt() // Vibrant Green
                else -> 0xFF9E9E9E.toInt()
            }
            
            if (timeframe == Timeframe.TODAY) {
                // Draw glow effect for TODAY view
                shadowPaint.color = moodColor and 0x50FFFFFF.toInt()
                canvas.drawCircle(point.x, point.y, pointRadius * 3f, shadowPaint)
                
                // Draw emoji with scale animation
                val scale = 0.5f + (animationProgress * 0.5f)
                textPaint.textSize = textSize * 1.5f * scale
                textPaint.color = 0xFF333333.toInt()
                canvas.drawText(point.emoji, point.x, point.y + textSize / 2f, textPaint)
                
                // Draw background circle
                pointPaint.color = 0x40FFFFFF.toInt()
                canvas.drawCircle(point.x, point.y, pointRadius * 2.5f, pointPaint)
            } else {
                // Draw glow effect
                shadowPaint.color = moodColor and 0x40FFFFFF.toInt()
                canvas.drawCircle(point.x, point.y, pointRadius * 2.5f, shadowPaint)
                
                // Draw colored point
                pointPaint.color = moodColor
                canvas.drawCircle(point.x, point.y, pointRadius * 1.8f, pointPaint)
                
                // Draw white border with glow
                pointPaint.color = 0xFFFFFFFF.toInt()
                pointPaint.style = Paint.Style.STROKE
                pointPaint.strokeWidth = 4f
                canvas.drawCircle(point.x, point.y, pointRadius * 1.8f, pointPaint)
                pointPaint.style = Paint.Style.FILL
                pointPaint.strokeWidth = 0f
                
                // Draw emoji above with scale
                val scale = 0.5f + (animationProgress * 0.5f)
                textPaint.textSize = textSize * 0.9f * scale
                textPaint.color = 0xFF333333.toInt()
                canvas.drawText(point.emoji, point.x, point.y - pointRadius * 3f - 8f, textPaint)
            }
        }
    }
    
    private fun drawLabels(canvas: Canvas) {
        if (chartPoints.isEmpty()) return
        
        textPaint.textSize = gridTextSize - 2
        textPaint.color = 0xFF888888.toInt()
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val dateFormat = when (timeframe) {
            Timeframe.TODAY -> SimpleDateFormat("HH:mm", Locale.getDefault())
            Timeframe.DAILY -> SimpleDateFormat("EEE", Locale.getDefault())
            Timeframe.WEEKLY -> SimpleDateFormat("MMM dd", Locale.getDefault())
            Timeframe.MONTHLY -> SimpleDateFormat("MMM", Locale.getDefault())
            Timeframe.YEARLY -> SimpleDateFormat("yyyy", Locale.getDefault())
        }
        
        val animatedPoints = chartPoints.take((chartPoints.size * animationProgress).toInt().coerceAtLeast(1))
        
        animatedPoints.forEachIndexed { index, point ->
            if (index % max(1, animatedPoints.size / 5) == 0) { // Show max 5 labels
                val label = dateFormat.format(Date(point.timestamp))
                canvas.drawText(
                    label,
                    point.x,
                    height - padding / 2 + 8,
                    textPaint
                )
            }
        }
        
        // Reset typeface
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 400
        val desiredHeight = 280 // Increased height for better chart visibility
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }
        
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && moodData.isNotEmpty()) {
            calculateChartPoints()
            invalidate()
        }
    }
}