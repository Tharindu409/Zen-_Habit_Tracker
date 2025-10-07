package com.example.zen.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.zen.R
import com.example.zen.models.Habit
import com.example.zen.repo.ZenRepository
import com.example.zen.util.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.zen.views.HabitProgressChartView
import com.example.zen.fragments.AddHabitBottomSheetFragment
import java.text.SimpleDateFormat
import java.util.*
// removed unused imports

class HabitDetailActivity : AppCompatActivity() {

    private lateinit var repository: ZenRepository
    private lateinit var habit: Habit
    
    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var tvEmoji: TextView
    private lateinit var tvHabitTitle: TextView
    private lateinit var tvDailyTarget: TextView
    private lateinit var tvStreakNumber: TextView
    private lateinit var tvProgressLarge: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var progressBar: ProgressBar
    
    // Quick action buttons
    private lateinit var btnQuick1: MaterialButton
    private lateinit var btnQuick2: MaterialButton
    private lateinit var btnQuick3: MaterialButton
    private lateinit var btnManualEntry: MaterialButton
    
    // Meditation timer components
    private lateinit var cardMeditationTimer: MaterialCardView
    private lateinit var chipGroupDuration: ChipGroup
    private lateinit var tvTimerDisplay: TextView
    private lateinit var btnStartPause: MaterialButton
    private lateinit var btnStop: MaterialButton
    
    // Chart and statistics
    private lateinit var chipGroupTimeframe: ChipGroup
    private lateinit var chartView: HabitProgressChartView
    
    // Action buttons
    private lateinit var btnEditHabit: MaterialButton
    private lateinit var btnDeleteHabit: MaterialButton
    
    // Timer variables
    private var meditationTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timerDurationMs = 5 * 60 * 1000L // Default 5 minutes
    private var remainingTimeMs = timerDurationMs

    companion object {
        private const val EXTRA_HABIT_ID = "habit_id"
        
        fun newIntent(context: Context, habitId: String): Intent {
            return Intent(context, HabitDetailActivity::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_detail)
        
        repository = ZenRepository.getInstance(this)
        
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
        if (habitId == null) {
            finish()
            return
        }
        
        habit = repository.getAllHabits().find { it.id == habitId } ?: run {
            finish()
            return
        }
        
        initViews()
        setupClickListeners()
        updateUI()
        updateMeditationTimerVisibility()
        
        // Delay chart initialization to ensure all views are properly set up
        chartView.post {
            updateChartStatistics()
        }
    }
    
    private fun initViews() {
        // Header
        btnBack = findViewById(R.id.btnBack)
        btnFavorite = findViewById(R.id.btnFavorite)
        
        // Top section
        tvEmoji = findViewById(R.id.tvEmoji)
        tvHabitTitle = findViewById(R.id.tvHabitTitle)
        tvDailyTarget = findViewById(R.id.tvDailyTarget)
        tvStreakNumber = findViewById(R.id.tvStreakNumber)
        tvProgressLarge = findViewById(R.id.tvProgressLarge)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        progressBar = findViewById(R.id.progressBar)
        
        // Quick actions
        btnQuick1 = findViewById(R.id.btnQuick1)
        btnQuick2 = findViewById(R.id.btnQuick2)
        btnQuick3 = findViewById(R.id.btnQuick3)
        btnManualEntry = findViewById(R.id.btnManualEntry)
        
        // Meditation timer
        cardMeditationTimer = findViewById(R.id.cardMeditationTimer)
        chipGroupDuration = findViewById(R.id.chipGroupDuration)
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnStop = findViewById(R.id.btnStop)
        
        // Chart and statistics
        chipGroupTimeframe = findViewById(R.id.chipGroupTimeframe)
        chartView = findViewById(R.id.chartView)
        
        // Action buttons
        btnEditHabit = findViewById(R.id.btnEditHabit)
        btnDeleteHabit = findViewById(R.id.btnDeleteHabit)
    }
    
    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        
        btnFavorite.setOnClickListener {
            habit = habit.copy(isStarred = !habit.isStarred)
            repository.updateHabit(habit)
            updateFavoriteIcon()
        }
        
        // Quick action buttons
        btnQuick1.setOnClickListener { addProgress(getQuickValue(0).toFloat()) }
        btnQuick2.setOnClickListener { addProgress(getQuickValue(1).toFloat()) }
        btnQuick3.setOnClickListener { addProgress(getQuickValue(2).toFloat()) }
        btnManualEntry.setOnClickListener { showManualEntryDialog() }
        
        // Meditation timer
        setupMeditationTimer()
        
        // Chart timeframe selection
        chipGroupTimeframe.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                updateChartForTimeframe(checkedIds[0])
            }
        }
        
        // Action buttons
        btnEditHabit.setOnClickListener { openEditHabit() }
        btnDeleteHabit.setOnClickListener { showDeleteConfirmation() }
    }
    
    private fun setupMeditationTimer() {
        // Duration chip selection
        chipGroupDuration.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chip5min -> setTimerDuration(5)
                    R.id.chip10min -> setTimerDuration(10)
                    R.id.chip15min -> setTimerDuration(15)
                    R.id.chip20min -> setTimerDuration(20)
                }
            }
        }
        
        btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        
        btnStop.setOnClickListener {
            stopTimer()
        }
    }
    
    private fun setTimerDuration(minutes: Int) {
        timerDurationMs = minutes * 60 * 1000L
        remainingTimeMs = timerDurationMs
        updateTimerDisplay()
    }
    
    private fun startTimer() {
        isTimerRunning = true
        btnStartPause.text = getString(R.string.pause)
        btnStartPause.setBackgroundTintList(
            resources.getColorStateList(R.color.warning, theme)
        )
        
        meditationTimer = object : CountDownTimer(remainingTimeMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                updateTimerDisplay()
            }
            
            override fun onFinish() {
                onTimerComplete()
            }
        }.start()
    }
    
    private fun pauseTimer() {
        isTimerRunning = false
        meditationTimer?.cancel()
        btnStartPause.text = getString(R.string.resume)
        btnStartPause.setBackgroundTintList(
            resources.getColorStateList(R.color.success, theme)
        )
    }
    
    private fun stopTimer() {
        isTimerRunning = false
        meditationTimer?.cancel()
        
        // Show partial session dialog if timer was running and some time passed
        val timeCompleted = timerDurationMs - remainingTimeMs
        if (timeCompleted > 0) {
            showPartialSessionDialog(timeCompleted)
        }
        
        resetTimer()
    }
    
    private fun resetTimer() {
        remainingTimeMs = timerDurationMs
        updateTimerDisplay()
        btnStartPause.text = getString(R.string.start_text)
        btnStartPause.setBackgroundTintList(
            resources.getColorStateList(R.color.success, theme)
        )
    }
    
    private fun onTimerComplete() {
        isTimerRunning = false
        
        // Log full meditation session
        val sessionMinutes = (timerDurationMs / (60 * 1000)).toInt()
        addProgress(sessionMinutes.toFloat())
        
        // Show completion dialog
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.meditation_complete_title))
            .setMessage(getString(R.string.meditation_complete_msg, sessionMinutes))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                resetTimer()
            }
            .show()
    }
    
    private fun showPartialSessionDialog(timeCompleted: Long) {
        val minutesCompleted = (timeCompleted / (60 * 1000)).toInt()
        if (minutesCompleted > 0) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.log_partial_title))
                .setMessage(getString(R.string.log_partial_msg, minutesCompleted))
                .setPositiveButton(getString(R.string.log_it)) { _, _ ->
                    addProgress(minutesCompleted.toFloat())
                }
                .setNegativeButton(getString(R.string.discard), null)
                .show()
        }
    }
    
    private fun updateTimerDisplay() {
        val minutes = (remainingTimeMs / (60 * 1000)).toInt()
        val seconds = ((remainingTimeMs % (60 * 1000)) / 1000).toInt()
    tvTimerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
    
    private fun updateMeditationTimerVisibility() {
        cardMeditationTimer.visibility = if (habit.title.contains("Meditate", ignoreCase = true)) View.VISIBLE else View.GONE
    }
    
    private fun getQuickValue(index: Int): Int {
        return when {
            habit.title.contains("Water", ignoreCase = true) -> when (index) {
                0 -> 250
                1 -> 500
                2 -> 1000
                else -> habit.defaultIncrement
            }
            habit.title.contains("Steps", ignoreCase = true) -> when (index) {
                0 -> 1000
                1 -> 2500
                2 -> 5000
                else -> habit.defaultIncrement
            }
            habit.title.contains("Meditate", ignoreCase = true) -> when (index) {
                0 -> 5
                1 -> 10
                2 -> 15
                else -> habit.defaultIncrement
            }
            else -> when (index) {
                0 -> (habit.defaultIncrement * 0.5).toInt()
                1 -> habit.defaultIncrement
                2 -> habit.defaultIncrement * 2
                else -> habit.defaultIncrement
            }
        }
    }
    
    private fun addProgress(value: Float) {
        repository.addTick(habit.id, value.toInt())
        updateProgressDisplay()
        updateChartStatistics()
    }
    
    private fun showManualEntryDialog() {
        val editText = EditText(this)
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
    editText.hint = getString(R.string.manual_entry_hint)

    MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.manual_entry_title))
            .setMessage(getString(R.string.manual_entry_msg, habit.unit.lowercase(Locale.getDefault())))
            .setView(editText)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val value = editText.text.toString().toFloatOrNull()
                if (value != null && value > 0) {
                    addProgress(value)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun updateUI() {
        tvEmoji.text = habit.emoji
        tvHabitTitle.text = habit.title
        // Removed habit type view

        updateFavoriteIcon()
        updateProgressDisplay()
        updateQuickButtons()
        
        // Update details
        // Update basic details
        tvEmoji.text = habit.emoji
        tvHabitTitle.text = habit.title
        tvDailyTarget.text = "Goal: ${habit.targetPerDay} ${habit.unit} daily"
        
        // Update favorite status
        updateFavoriteIcon()
        
        // Update today's progress
        updateProgressDisplay()
        
        // Set up quick action buttons
        updateQuickButtons()
    }
    
    private fun updateFavoriteIcon() {
        btnFavorite.setImageResource(
            if (habit.isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )
    }
    
    private fun updateProgressDisplay() {
        val today = DateUtils.nowDateString()
        val todayTicks = repository.getTicksForDate(today)
        val currentProgress = todayTicks.find { it.habitId == habit.id }?.amount ?: 0
        val progressPercent = if (habit.targetPerDay > 0) {
            ((currentProgress * 100) / habit.targetPerDay).coerceAtMost(100)
        } else 0
        
        tvStreakNumber.text = calculateCurrentStreak().toString()
    tvProgressLarge.text = getString(R.string.habits_progress, currentProgress, habit.targetPerDay)
    tvProgressPercent.text = getString(R.string.percent_format, progressPercent)
        progressBar.progress = progressPercent
    }
    
    private fun updateQuickButtons() {
        val values = listOf(getQuickValue(0), getQuickValue(1), getQuickValue(2))
        btnQuick1.text = "+${values[0]}"
        btnQuick2.text = "+${values[1]}"
        btnQuick3.text = "+${values[2]}"
    }
    
    private fun calculateCurrentStreak(): Int {
        val calendar = Calendar.getInstance()
        var streak = 0
        
        while (true) {
            val dateStr = DateUtils.calendarToDateString(calendar)
            val dayTicks = repository.getTicksForDate(dateStr)
            val dayProgress = dayTicks.find { it.habitId == habit.id }?.amount ?: 0
            
            if (dayProgress >= habit.targetPerDay) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun updateChartStatistics() {
        try {
            // Update chart with current timeframe
            val selectedChipId = chipGroupTimeframe.checkedChipId
            if (selectedChipId != View.NO_ID) {
                val timeframe = when (selectedChipId) {
                    R.id.chipWeekly -> "Weekly"
                    R.id.chipMonthly -> "Monthly"
                    else -> "Weekly"
                }
                val chartData = generateChartData(timeframe)
                chartView.setChartData(chartData, timeframe)
            } else {
                // Default to weekly view
                chipGroupTimeframe.check(R.id.chipWeekly)
                val chartData = generateChartData("Weekly")
                chartView.setChartData(chartData, "Weekly")
            }
        } catch (e: Exception) {
            // Log error and continue without crashing
            e.printStackTrace()
            // Set default values if there's an error
        // Removed stats view
        // Removed stats view
        // Removed stats view
        // Removed stats view
        }
    }
    
    private fun calculateTotalSessions(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30) // Look back 30 days
        
        var sessions = 0
        repeat(30) {
            val dateStr = DateUtils.calendarToDateString(calendar)
            val dayTicks = repository.getTicksForDate(dateStr)
            val progress = dayTicks.find { it.habitId == habit.id }?.amount ?: 0
            if (progress >= habit.targetPerDay) {
                sessions++
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return sessions
    }
    
    private fun calculateDailyAverage(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Look back 7 days
        
        var totalProgress = 0
        repeat(7) {
            val dateStr = DateUtils.calendarToDateString(calendar)
            val dayTicks = repository.getTicksForDate(dateStr)
            totalProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return totalProgress / 7
    }
    
    private fun calculateBestDay(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30) // Look back 30 days
        
        var bestProgress = 0
        repeat(30) {
            val dateStr = DateUtils.calendarToDateString(calendar)
            val dayTicks = repository.getTicksForDate(dateStr)
            val progress = dayTicks.find { it.habitId == habit.id }?.amount ?: 0
            if (progress > bestProgress) {
                bestProgress = progress
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return bestProgress
    }
    
    private fun updateChartForTimeframe(chipId: Int) {
        try {
            val timeframe = when (chipId) {
                R.id.chipDaily -> "Daily"
                R.id.chipWeekly -> "Weekly"
                R.id.chipMonthly -> "Monthly"
                R.id.chipYearly -> "Yearly"
                else -> "Daily"
            }
            
            val chartData = generateChartData(timeframe)
            chartView.setChartData(chartData, timeframe)
            
            // Update statistics based on timeframe without recursive calls
            when (chipId) {
                R.id.chipDaily -> updateDailyStatisticsOnly()
                R.id.chipWeekly -> updateWeeklyStatisticsOnly()
                R.id.chipMonthly -> updateMonthlyStatisticsOnly()
                R.id.chipYearly -> updateYearlyStatisticsOnly()
            }
        } catch (e: Exception) {
            // Log error and set empty chart data
            e.printStackTrace()
            chartView.setChartData(emptyList(), "Daily")
        }
    }
    
    private fun generateChartData(timeframe: String): List<HabitProgressChartView.ChartDataPoint> {
        return try {
            val calendar = Calendar.getInstance()
            val data = mutableListOf<HabitProgressChartView.ChartDataPoint>()
            
            when (timeframe) {
            "Daily" -> {
                // Show last 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                repeat(7) {
                    val dateStr = DateUtils.calendarToDateString(calendar)
                    val dayTicks = repository.getTicksForDate(dateStr)
                    val progress = dayTicks.find { it.habitId == habit.id }?.amount?.toFloat() ?: 0f
                    
                    val dayLabel = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "Sun"
                        Calendar.MONDAY -> "Mon"
                        Calendar.TUESDAY -> "Tue"
                        Calendar.WEDNESDAY -> "Wed"
                        Calendar.THURSDAY -> "Thu"
                        Calendar.FRIDAY -> "Fri"
                        Calendar.SATURDAY -> "Sat"
                        else -> ""
                    }
                    
                    data.add(HabitProgressChartView.ChartDataPoint(
                        label = dayLabel,
                        value = progress,
                        target = habit.targetPerDay.toFloat(),
                        date = dateStr
                    ))
                    
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            "Weekly" -> {
                // Show last 4 weeks
                calendar.add(Calendar.WEEK_OF_YEAR, -3)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                
                repeat(4) {
                    val weekStart = calendar.clone() as Calendar
                    var weekTotal = 0f
                    
                    repeat(7) {
                        val dateStr = DateUtils.calendarToDateString(weekStart)
                        val dayTicks = repository.getTicksForDate(dateStr)
                        weekTotal += dayTicks.find { it.habitId == habit.id }?.amount?.toFloat() ?: 0f
                        weekStart.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    
                    val weekLabel = "W${calendar.get(Calendar.WEEK_OF_YEAR)}"
                    
                    data.add(HabitProgressChartView.ChartDataPoint(
                        label = weekLabel,
                        value = weekTotal,
                        target = habit.targetPerDay.toFloat() * 7,
                        date = DateUtils.calendarToDateString(calendar)
                    ))
                    
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            
            "Monthly" -> {
                // Show last 6 months
                calendar.add(Calendar.MONTH, -5)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                
                repeat(6) {
                    val monthStart = calendar.clone() as Calendar
                    var monthTotal = 0f
                    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    
                    repeat(daysInMonth) {
                        val dateStr = DateUtils.calendarToDateString(monthStart)
                        val dayTicks = repository.getTicksForDate(dateStr)
                        monthTotal += dayTicks.find { it.habitId == habit.id }?.amount?.toFloat() ?: 0f
                        monthStart.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    
                    val monthLabel = when (calendar.get(Calendar.MONTH)) {
                        Calendar.JANUARY -> "Jan"
                        Calendar.FEBRUARY -> "Feb"
                        Calendar.MARCH -> "Mar"
                        Calendar.APRIL -> "Apr"
                        Calendar.MAY -> "May"
                        Calendar.JUNE -> "Jun"
                        Calendar.JULY -> "Jul"
                        Calendar.AUGUST -> "Aug"
                        Calendar.SEPTEMBER -> "Sep"
                        Calendar.OCTOBER -> "Oct"
                        Calendar.NOVEMBER -> "Nov"
                        Calendar.DECEMBER -> "Dec"
                        else -> ""
                    }
                    
                    data.add(HabitProgressChartView.ChartDataPoint(
                        label = monthLabel,
                        value = monthTotal,
                        target = habit.targetPerDay.toFloat() * daysInMonth,
                        date = DateUtils.calendarToDateString(calendar)
                    ))
                    
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            
            "Yearly" -> {
                // Show last 3 years
                calendar.add(Calendar.YEAR, -2)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                
                repeat(3) {
                    val yearStart = calendar.clone() as Calendar
                    var yearTotal = 0f
                    val daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                    
                    repeat(daysInYear) {
                        val dateStr = DateUtils.calendarToDateString(yearStart)
                        val dayTicks = repository.getTicksForDate(dateStr)
                        yearTotal += dayTicks.find { it.habitId == habit.id }?.amount?.toFloat() ?: 0f
                        yearStart.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    
                    val yearLabel = calendar.get(Calendar.YEAR).toString()
                    
                    data.add(HabitProgressChartView.ChartDataPoint(
                        label = yearLabel,
                        value = yearTotal,
                        target = habit.targetPerDay.toFloat() * daysInYear,
                        date = DateUtils.calendarToDateString(calendar)
                    ))
                    
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }
        
        data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun updateDailyStatisticsOnly() {
        // Calculate daily-specific statistics
        val currentStreak = calculateCurrentStreak()
        val totalSessions = calculateTotalSessions()
        val dailyAverage = calculateDailyAverage()
        val bestDay = calculateBestDay()
        
        // Removed stats view
        // Removed stats view
        // Removed stats view
        // Removed stats view
    }
    
    private fun updateWeeklyStatisticsOnly() {
        // Calculate weekly-specific statistics
        val currentStreak = calculateWeeklyStreak()
        val totalSessions = calculateWeeklySessions()
        val weeklyAverage = calculateWeeklyAverage()
        val bestWeek = calculateBestWeek()
        
        // Removed stats view
        // Removed stats view
        // Removed stats view
        // Removed stats view
    }
    
    private fun updateMonthlyStatisticsOnly() {
        // Calculate monthly-specific statistics
        val currentStreak = calculateMonthlyStreak()
        val totalSessions = calculateMonthlySessions()
        val monthlyAverage = calculateMonthlyAverage()
        val bestMonth = calculateBestMonth()
        
        // Removed stats view
        // Removed stats view
        // Removed stats view
        // Removed stats view
    }
    
    private fun updateYearlyStatisticsOnly() {
        // Calculate yearly-specific statistics
        val currentStreak = calculateYearlyStreak()
        val totalSessions = calculateYearlySessions()
        val yearlyAverage = calculateYearlyAverage()
        val bestYear = calculateBestYear()
        
        // Removed stats view
        // Removed stats view
        // Removed stats view
        // Removed stats view
    }
    
    // Additional calculation methods for different timeframes
    private fun calculateWeeklyStreak(): Int {
        // Calculate streak in weeks
        return calculateCurrentStreak() / 7
    }
    
    private fun calculateWeeklySessions(): Int {
        // Calculate total weeks with sessions in last 12 weeks
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -12)
        
        var weekSessions = 0
        repeat(12) {
            var weekProgress = 0
            repeat(7) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                weekProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (weekProgress >= habit.targetPerDay * 7) {
                weekSessions++
            }
            calendar.add(Calendar.DAY_OF_YEAR, -7) // Reset to start of week
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return weekSessions
    }
    
    private fun calculateWeeklyAverage(): Int {
        // Calculate average progress per week
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -4)
        
        var totalProgress = 0
        repeat(4) {
            repeat(7) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                totalProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return totalProgress / 4
    }
    
    private fun calculateBestWeek(): Int {
        // Calculate best week progress
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -12)
        
        var bestProgress = 0
        repeat(12) {
            var weekProgress = 0
            repeat(7) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                weekProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (weekProgress > bestProgress) {
                bestProgress = weekProgress
            }
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return bestProgress
    }
    
    private fun calculateMonthlyStreak(): Int {
        return calculateCurrentStreak() / 30
    }
    
    private fun calculateMonthlySessions(): Int {
        // Calculate total months with sessions in last 6 months
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -6)
        
        var monthSessions = 0
        repeat(6) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            var monthProgress = 0
            repeat(daysInMonth) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                monthProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (monthProgress >= habit.targetPerDay * daysInMonth) {
                monthSessions++
            }
            calendar.add(Calendar.DAY_OF_YEAR, -daysInMonth)
            calendar.add(Calendar.MONTH, 1)
        }
        return monthSessions
    }
    
    private fun calculateMonthlyAverage(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -3)
        
        var totalProgress = 0
        repeat(3) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            repeat(daysInMonth) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                totalProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            calendar.add(Calendar.DAY_OF_YEAR, -daysInMonth)
            calendar.add(Calendar.MONTH, 1)
        }
        return totalProgress / 3
    }
    
    private fun calculateBestMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -6)
        
        var bestProgress = 0
        repeat(6) {
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            var monthProgress = 0
            repeat(daysInMonth) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                monthProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (monthProgress > bestProgress) {
                bestProgress = monthProgress
            }
            calendar.add(Calendar.DAY_OF_YEAR, -daysInMonth)
            calendar.add(Calendar.MONTH, 1)
        }
        return bestProgress
    }
    
    private fun calculateYearlyStreak(): Int {
        return calculateCurrentStreak() / 365
    }
    
    private fun calculateYearlySessions(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -2)
        
        var yearSessions = 0
        repeat(2) {
            val daysInYear = if (calendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
            var yearProgress = 0
            repeat(daysInYear) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                yearProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (yearProgress >= habit.targetPerDay * daysInYear) {
                yearSessions++
            }
            calendar.add(Calendar.DAY_OF_YEAR, -daysInYear)
            calendar.add(Calendar.YEAR, 1)
        }
        return yearSessions
    }
    
    private fun calculateYearlyAverage(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        
        val daysInYear = if (calendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
        var totalProgress = 0
        repeat(daysInYear) {
            val dateStr = DateUtils.calendarToDateString(calendar)
            val dayTicks = repository.getTicksForDate(dateStr)
            totalProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return totalProgress / daysInYear
    }
    
    private fun calculateBestYear(): Int {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -2)
        
        var bestProgress = 0
        repeat(2) {
            val daysInYear = if (calendar.getActualMaximum(Calendar.DAY_OF_YEAR) == 366) 366 else 365
            var yearProgress = 0
            repeat(daysInYear) {
                val dateStr = DateUtils.calendarToDateString(calendar)
                val dayTicks = repository.getTicksForDate(dateStr)
                yearProgress += dayTicks.find { it.habitId == habit.id }?.amount ?: 0
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (yearProgress > bestProgress) {
                bestProgress = yearProgress
            }
            calendar.add(Calendar.DAY_OF_YEAR, -daysInYear)
            calendar.add(Calendar.YEAR, 1)
        }
        return bestProgress
    }

    private fun openEditHabit() {
        val editFragment = AddHabitBottomSheetFragment.newInstance(
            onHabitCreated = {
                // Refresh the habit data and UI
                habit = repository.getAllHabits().find { it.id == habit.id } ?: habit
                updateUI()
                updateMeditationTimerVisibility()
                chartView.post {
                    updateChartStatistics()
                }
            },
            habitToEdit = habit
        )
        
    editFragment.show(supportFragmentManager, getString(R.string.edit_habit_tag))
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_habit))
            .setMessage(getString(R.string.confirm_delete_habit))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteHabit()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun deleteHabit() {
        repository.deleteHabit(habit.id)
    Toast.makeText(this, getString(R.string.habit_deleted), Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        meditationTimer?.cancel()
    }
}