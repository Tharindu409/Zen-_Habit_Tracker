package com.example.zen.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.zen.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom view for displaying profile statistics with circular progress indicators
 * Shows: Total Habits, Active Streaks, Mood Entries, Days Active
 */
class ProfileStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class StatItem(
        val label: String,
        val value: Int,
        val maxValue: Int,
        val emoji: String,
        val color: Int
    )

    private var stats = listOf<StatItem>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var animationProgress = 0f
    private var animator: ValueAnimator? = null

    private val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val accentColor = ContextCompat.getColor(context, R.color.accent)
    
    init {
        textPaint.textAlign = Paint.Align.CENTER
        emojiPaint.textAlign = Paint.Align.CENTER
    }

    fun setStats(newStats: List<StatItem>) {
        stats = newStats
        animateIn()
    }

    private fun animateIn() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
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

        if (stats.isEmpty()) return

        if (stats.size == 2) {
            // Two-row layout: left big text, right circular chart
            val rowHeight = height / 2f
            val rightPadding = 24f
            val leftPadding = 24f
            val chartRadius = (rowHeight * 0.35f).coerceAtMost(80f)

            val oldAlign = textPaint.textAlign
            textPaint.textAlign = Paint.Align.LEFT

            for (i in 0..1) {
                val stat = stats[i]
                val centerY = rowHeight * i + rowHeight / 2f
                val centerX = width - rightPadding - chartRadius

                // Draw chart on the right
                drawChart(canvas, stat, centerX, centerY, chartRadius)

                // Draw value and label on the left
                val valueY = centerY - 8f
                textPaint.color = Color.parseColor("#1E1E1E")
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textPaint.textSize = 56f
                canvas.drawText(stat.value.toString(), leftPadding, valueY, textPaint)

                textPaint.color = Color.parseColor("#6B7280")
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textPaint.textSize = 24f
                val labelY = valueY + 30f
                canvas.drawText(stat.label, leftPadding, labelY, textPaint)
            }

            textPaint.textAlign = oldAlign
            return
        }

        // Fallback: grid layout (2x2)
        val itemWidth = width / 2f
        val itemHeight = height / 2f
        stats.forEachIndexed { index, stat ->
            val col = index % 2
            val row = index / 2
            val centerX = itemWidth * col + itemWidth / 2
            val centerY = itemHeight * row + itemHeight / 2
            drawStatItem(canvas, stat, centerX, centerY, itemWidth.coerceAtMost(itemHeight))
        }
    }

    private fun drawChart(canvas: Canvas, stat: StatItem, centerX: Float, centerY: Float, radius: Float) {
        val strokeWidth = 12f
        // Background circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = Color.parseColor("#F0F0F0")
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Progress arc
        val progress = (stat.value.toFloat() / stat.maxValue.coerceAtLeast(1)).coerceIn(0f, 1f)
        val animatedProgress = progress * animationProgress
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = stat.color
        val sweepAngle = animatedProgress * 360f
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            sweepAngle,
            false,
            paint
        )

        // Emoji in center
        emojiPaint.textSize = 28f
        val emojiY = centerY + 10f
        canvas.drawText(stat.emoji, centerX, emojiY, emojiPaint)

        // Optional glow
        if (animatedProgress > 0.5f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth + 4f
            paint.color = adjustAlpha(stat.color, 0.3f)
            paint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                -90f,
                sweepAngle,
                false,
                paint
            )
            paint.maskFilter = null
        }
    }

    private fun drawStatItem(canvas: Canvas, stat: StatItem, centerX: Float, centerY: Float, size: Float) {
        val radius = (size * 0.28f).coerceAtMost(100f)
        val strokeWidth = 12f
        
        // Draw background circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = Color.parseColor("#F0F0F0")
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // Draw progress arc with gradient
        val progress = (stat.value.toFloat() / stat.maxValue.coerceAtLeast(1)).coerceIn(0f, 1f)
        val animatedProgress = progress * animationProgress
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = stat.color
        
        val sweepAngle = animatedProgress * 360f
        canvas.drawArc(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius,
            -90f,
            sweepAngle,
            false,
            paint
        )
        
        // Draw emoji in center
        emojiPaint.textSize = 32f
        val emojiY = centerY + 12f
        canvas.drawText(stat.emoji, centerX, emojiY, emojiPaint)
        
        // Draw value
        textPaint.color = Color.parseColor("#1E1E1E")
        textPaint.textSize = 40f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val valueY = centerY + radius + 40f
        canvas.drawText(stat.value.toString(), centerX, valueY, textPaint)
        
        // Draw label
        textPaint.color = Color.parseColor("#6B7280")
        textPaint.textSize = 20f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val labelY = valueY + 26f
        canvas.drawText(stat.label, centerX, labelY, textPaint)
        
        // Draw glow effect on progress
        if (animatedProgress > 0.5f) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth + 4f
            paint.color = adjustAlpha(stat.color, 0.3f)
            paint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                -90f,
                sweepAngle,
                false,
                paint
            )
            paint.maskFilter = null
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 400
        val desiredHeight = 280

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
