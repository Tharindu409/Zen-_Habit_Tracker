package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zen.R
// removed unused model imports
import com.example.zen.repo.ZenRepository
import com.example.zen.util.DateUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigationrail.NavigationRailView
import android.widget.ProgressBar
import android.widget.TextView
import java.util.*
import kotlin.math.min

class HomeActivity : AppCompatActivity(), com.example.zen.repo.ZenRepository.DataChangeListener {

    private lateinit var repository: ZenRepository
    private lateinit var tvGreeting: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var tvCompletionRate: TextView
    private lateinit var tvDailyQuote: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvHabitsProgress: TextView
    private lateinit var progressRingHabits: ProgressBar
    private lateinit var tvWaterAmount: TextView
    private lateinit var btnWaterPlus: MaterialButton
    private lateinit var tvStepsAmount: TextView
    private lateinit var btnStepsAdd: MaterialButton
    private lateinit var btnOpenMood: MaterialButton
    
    // Quick action buttons
    private lateinit var btnQuickLogHabit: android.view.View
    private lateinit var btnQuickLogMood: android.view.View
    private lateinit var btnQuickAddWater: android.view.View
    private lateinit var btnQuickAddSteps: android.view.View
    
    private var bottomNav: BottomNavigationView? = null
    private var navRail: NavigationRailView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        repository = ZenRepository.getInstance(this)
        
        initViews()
        setupNavigation()
        setupClickListeners()
        loadData()
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvStreakCount = findViewById(R.id.tvStreakCount)
        tvCompletionRate = findViewById(R.id.tvCompletionRate)
        tvDailyQuote = findViewById(R.id.tvDailyQuote)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvHabitsProgress = findViewById(R.id.tvHabitsProgress)
        progressRingHabits = findViewById(R.id.progressRingHabits)
        tvWaterAmount = findViewById(R.id.tvWaterAmount)
        btnWaterPlus = findViewById(R.id.btnWaterPlus)
        tvStepsAmount = findViewById(R.id.tvStepsAmount)
        btnStepsAdd = findViewById(R.id.btnStepsAdd)
        btnOpenMood = findViewById(R.id.btnOpenMood)
        
        // Quick action buttons
        btnQuickLogHabit = findViewById(R.id.btnQuickLogHabit)
        btnQuickLogMood = findViewById(R.id.btnQuickLogMood)
        btnQuickAddWater = findViewById(R.id.btnQuickAddWater)
        btnQuickAddSteps = findViewById(R.id.btnQuickAddSteps)
        
        // Navigation components (one will be null depending on orientation)
        bottomNav = findViewById(R.id.bottomNav)
        navRail = findViewById(R.id.navRail)
    }

    private fun setupNavigation() {
        val navigationListener = { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.menu_habits -> {
                    startActivity(Intent(this, HabitsActivity::class.java))
                    true
                }
                R.id.menu_mood -> {
                    startActivity(Intent(this, MoodActivity::class.java))
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        bottomNav?.setOnItemSelectedListener(navigationListener)
        navRail?.setOnItemSelectedListener(navigationListener)
        
        // Set home as selected
        bottomNav?.selectedItemId = R.id.menu_home
        navRail?.selectedItemId = R.id.menu_home
    }

    private fun setupClickListeners() {
        // Water button
        btnWaterPlus.setOnClickListener {
            val waterHabit = repository.getHabit("habit_water")
            waterHabit?.let { habit ->
                repository.addTick(habit.id, habit.defaultIncrement)
                updateWaterCard()
                updateHabitsProgress()
                updateStats()
            }
        }
        
        // Steps button
        btnStepsAdd.setOnClickListener {
            val stepsHabit = repository.getHabit("habit_steps")
            stepsHabit?.let { habit ->
                repository.addTick(habit.id, habit.defaultIncrement)
                updateStepsCard()
                updateHabitsProgress()
                updateStats()
            }
        }
        
        // Mood button
        btnOpenMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }
        
        // Quick action buttons
        btnQuickLogHabit.setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
        
        btnQuickLogMood.setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }
        
        btnQuickAddWater.setOnClickListener {
            val waterHabit = repository.getHabit("habit_water")
            waterHabit?.let { habit ->
                repository.addTick(habit.id, habit.defaultIncrement)
                updateWaterCard()
                updateHabitsProgress()
                updateStats()
            }
        }
        
        btnQuickAddSteps.setOnClickListener {
            val stepsHabit = repository.getHabit("habit_steps")
            stepsHabit?.let { habit ->
                repository.addTick(habit.id, habit.defaultIncrement)
                updateStepsCard()
                updateHabitsProgress()
                updateStats()
            }
        }
        
        // Card click listeners
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardWater).setOnClickListener {
            val intent = Intent(this, HabitsActivity::class.java)
            intent.putExtra("highlightHabitId", "habit_water")
            startActivity(intent)
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardHabits).setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
    }

    private fun loadData() {
        updateGreeting()
        updateStats()
        updateDailyQuote()
        updateHabitsProgress()
        updateWaterCard()
        updateStepsCard()
    }

    private fun updateGreeting() {
        val profile = repository.getUserProfile()
        val name = profile?.name ?: "User"
        val emoji = profile?.avatarEmoji ?: "😊"
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good morning, $emoji $name"
            in 12..16 -> "Good afternoon, $emoji $name"
            in 17..20 -> "Good evening, $emoji $name"
            else -> "Good night, $emoji $name"
        }
        
        tvGreeting.text = greeting
        tvSubtitle.text = "Let's make today amazing"
    }
    
    private fun updateStats() {
        // Calculate current streak
        val habits = repository.getAllHabits().filter { it.enabled }
        val streak = calculateStreak(habits)
        tvStreakCount.text = streak.toString()
        
        // Calculate weekly completion rate
        val weekRate = calculateWeeklyCompletionRate(habits)
        tvCompletionRate.text = "$weekRate%"
    }
    
    private fun calculateStreak(habits: List<com.example.zen.models.Habit>): Int {
        if (habits.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        
        // Check backwards from today
        for (i in 0 until 365) { // Max check 1 year
            calendar.add(Calendar.DAY_OF_YEAR, if (i == 0) 0 else -1)
            val dateString = DateUtils.calendarToDateString(calendar)
            val todayTicks = repository.getTicksForDate(dateString)
            
            // Count how many habits were completed on this day
            val completedCount = habits.count { habit ->
                val tick = todayTicks.firstOrNull { it.habitId == habit.id }
                val progress = (tick?.amount ?: 0).toFloat() / habit.targetPerDay
                progress >= 1.0f
            }
            
            // If at least one habit was completed, continue streak
            if (completedCount > 0) {
                streak++
            } else if (i > 0) {
                // Break streak only after first day (allow for fresh start today)
                break
            }
        }
        
        return streak
    }
    
    private fun calculateWeeklyCompletionRate(habits: List<com.example.zen.models.Habit>): Int {
        if (habits.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        var totalPossible = 0
        var totalCompleted = 0
        
        // Check last 7 days
        for (i in 0 until 7) {
            calendar.add(Calendar.DAY_OF_YEAR, if (i == 0) 0 else -1)
            val dateString = DateUtils.calendarToDateString(calendar)
            val todayTicks = repository.getTicksForDate(dateString)
            
            habits.forEach { habit ->
                totalPossible++
                val tick = todayTicks.firstOrNull { it.habitId == habit.id }
                val progress = (tick?.amount ?: 0).toFloat() / habit.targetPerDay
                if (progress >= 1.0f) {
                    totalCompleted++
                }
            }
        }
        
        return if (totalPossible > 0) (totalCompleted * 100 / totalPossible) else 0
    }
    
    private fun updateDailyQuote() {
        val quotes = listOf(
            "Success is the sum of small efforts repeated day in and day out.",
            "You're stronger than you think.",
            "Progress, not perfection.",
            "One habit at a time.",
            "Keep going, future you will thank you.",
            "Consistency beats intensity.",
            "Believe you can and you're halfway there.",
            "Small changes can make a big difference.",
            "Every day is a new beginning.",
            "The secret of change is to focus all your energy on building the new."
        )
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val index = hour % quotes.size
        tvDailyQuote.text = quotes[index]
    }

    private fun updateHabitsProgress() {
        val habits = repository.getAllHabits().filter { it.enabled }
        val today = DateUtils.nowDateString()
        val todayTicks = repository.getTicksForDate(today)
        
        val completedCount = habits.count { habit ->
            val tick = todayTicks.firstOrNull { it.habitId == habit.id }
            val progress = (tick?.amount ?: 0).toFloat() / habit.targetPerDay
            progress >= 1.0f
        }
        
        tvHabitsProgress.text = "$completedCount / ${habits.size}"
        
        // Use average of capped per-habit completion for the ring
        val overallPercent = computeAverageCompletionPercent(habits, todayTicks)
        progressRingHabits.progress = overallPercent
        
        val message = when {
            overallPercent <= 25 -> "Let's get started!"
            overallPercent in 26..50 -> "Making progress!"
            overallPercent in 51..99 -> "Almost there!"
            else -> "Completed!"
        }
        tvProgressPercent.text = message
    }

    private fun computeAverageCompletionPercent(
        habits: List<com.example.zen.models.Habit>,
        todayTicks: List<com.example.zen.models.HabitTick>
    ): Int {
        if (habits.isEmpty()) return 0
        var sum = 0f
        habits.forEach { h ->
            val done = todayTicks.firstOrNull { it.habitId == h.id }?.amount ?: 0
            val ratio = min(done.toFloat() / h.targetPerDay, 1f)
            sum += ratio
        }
        val avg = (sum / habits.size) * 100f
        return avg.toInt()
    }

    private fun updateWaterCard() {
        val waterHabit = repository.getHabit("habit_water")
        if (waterHabit != null) {
            val today = DateUtils.nowDateString()
            val tick = repository.getTicksForDate(today).firstOrNull { it.habitId == waterHabit.id }
            val current = tick?.amount ?: 0
            
            tvWaterAmount.text = "$current / ${waterHabit.targetPerDay} mL"
        }
    }

    private fun updateStepsCard() {
        val stepsHabit = repository.getHabit("habit_steps")
        if (stepsHabit != null) {
            val today = DateUtils.nowDateString()
            val tick = repository.getTicksForDate(today).firstOrNull { it.habitId == stepsHabit.id }
            val current = tick?.amount ?: 0
            
            // Format with comma separator for thousands
            val formattedCurrent = String.format("%,d", current)
            val formattedTarget = String.format("%,d", stepsHabit.targetPerDay)
            tvStepsAmount.text = "$formattedCurrent / $formattedTarget"
        }
    }

    override fun onResume() {
        super.onResume()
        loadData() // Refresh data when returning to the activity
        repository.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        repository.removeListener(this)
    }

    override fun onTicksChanged() {
        // Called from repository on any tick changes; refresh dependent UI
        runOnUiThread {
            updateHabitsProgress()
            updateStats()
            updateWaterCard()
            updateStepsCard()
        }
    }
}
