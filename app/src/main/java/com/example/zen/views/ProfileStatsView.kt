package com.example.zen.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.zen.R

/**
 * Custom view for displaying profile statistics with text and emoji
 * Shows: Total Habits, Best Streak with progress bars
 */
class ProfileStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Helpers to convert dp/sp to pixels for consistent sizing across densities
    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity

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

        // Two-row text + emoji layout (no charts)
        val rowHeight = height / stats.size.toFloat()
        // Tight paddings
        val leftPadding = dp(12f)
        val rightPadding = dp(12f)
        
        val oldAlign = textPaint.textAlign
        textPaint.textAlign = Paint.Align.LEFT

        for (i in stats.indices) {
            val stat = stats[i]
            val centerY = rowHeight * i + rowHeight / 2f
            
            // Draw emoji on the left
            emojiPaint.textSize = sp(28f)
            emojiPaint.textAlign = Paint.Align.LEFT
            // Place emoji slightly below center baseline for optical alignment
            val emojiY = centerY + dp(6f)
            canvas.drawText(stat.emoji, leftPadding, emojiY, emojiPaint)
            
            // Draw value (big number) next to emoji
            val valueX = leftPadding + dp(40f)
            textPaint.color = Color.parseColor("#111827") // darker for better contrast
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = sp(26f)
            textPaint.textAlign = Paint.Align.LEFT
            // Align value to row center baseline
            val valueY = centerY
            canvas.drawText(stat.value.toString(), valueX, valueY, textPaint)
            
            // Draw label below
            textPaint.color = Color.parseColor("#374151") // improve readability
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = sp(14f)
            val labelY = valueY + dp(18f)
            canvas.drawText(stat.label, valueX, labelY, textPaint)
            
            // Draw progress indicator (simple bar on the right)
            val targetBarWidth = dp(140f)
            val barWidth = minOf(targetBarWidth, width * 0.32f)
            val barHeight = dp(10f)
            val barX = width - rightPadding - barWidth
            val barY = centerY - barHeight / 2f
            
            // Background bar
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#E5E7EB")
            val rectBg = RectF(barX, barY, barX + barWidth, barY + barHeight)
            canvas.drawRoundRect(rectBg, barHeight / 2f, barHeight / 2f, paint)
            
            // Progress bar
            val progress = (stat.value.toFloat() / stat.maxValue.coerceAtLeast(1)).coerceIn(0f, 1f)
            val animatedProgress = progress * animationProgress
            paint.color = stat.color
            val rectProgress = RectF(barX, barY, barX + barWidth * animatedProgress, barY + barHeight)
            canvas.drawRoundRect(rectProgress, barHeight / 2f, barHeight / 2f, paint)
        }

        textPaint.textAlign = oldAlign
        emojiPaint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredWidth = dp(400f).toInt()
    // Reduce height to tighten spacing between the two rows
    val desiredHeight = dp(220f).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}

