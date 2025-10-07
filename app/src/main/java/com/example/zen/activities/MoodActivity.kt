package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zen.R
import com.example.zen.adapters.MoodAdapter
import com.example.zen.dialogs.MoodBottomSheetDialog
import com.example.zen.models.MoodEntry
import com.example.zen.repo.ZenRepository
import com.example.zen.views.MoodChartView
import com.example.zen.views.MoodStatsView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigationrail.NavigationRailView
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MoodActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null
    private var navRail: NavigationRailView? = null
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var layoutEmptyMoods: View
    private lateinit var moodChartView: MoodChartView
    private lateinit var moodStatsView: MoodStatsView
    private lateinit var chipGroupTimelineMood: ChipGroup
    private lateinit var tvMoodCount: TextView
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var repository: ZenRepository
    private var lastAddedMoodId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)
        
        repository = ZenRepository.getInstance(this)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        moodRecyclerView = findViewById(R.id.rvMoodHistory)
        fabAddMood = findViewById(R.id.fabAddMood)
        layoutEmptyMoods = findViewById(R.id.layoutEmptyMoods)
        moodChartView = findViewById(R.id.moodChartView)
        moodStatsView = findViewById(R.id.moodStatsView)
        chipGroupTimelineMood = findViewById(R.id.chipGroupTimelineMood)
        tvMoodCount = findViewById(R.id.tvMoodCount)
        
        setupNavigation()
        setupMoodHistory()
        setupMoodChart()
        setupMoodStats()
        setupFab()
        loadMoodHistory()
    }

    private fun setupFab() {
        fabAddMood.setOnClickListener {
            showMoodBottomSheet()
        }
    }

    private fun showMoodBottomSheet(moodEntry: MoodEntry? = null) {
        val dialog = MoodBottomSheetDialog.newInstance(
            moodEntry = moodEntry,
            onSave = { mood ->
                if (moodEntry != null) {
                    // Update existing mood
                    repository.updateMood(mood)
                } else {
                    // Add new mood
                    repository.addMood(mood)
                }
                loadMoodHistory()
                
                val message = getString(R.string.mood_saved_successfully)
                Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
            },
            onDelete = if (moodEntry != null) { { moodId ->
                repository.deleteMood(moodId)
                loadMoodHistory()
                
                Snackbar.make(coordinatorLayout, R.string.mood_deleted, Snackbar.LENGTH_SHORT)
                    .show()
            } } else null
        )
        
        dialog.show(supportFragmentManager, "MoodBottomSheet")
    }

    private fun setupMoodHistory() {
        moodAdapter = MoodAdapter { moodEntry ->
            showMoodBottomSheet(moodEntry)
        }
        
        moodRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoodActivity)
            adapter = moodAdapter
        }
    }

    private fun loadMoodHistory() {
        val allMoods = repository.getAllMoods().sortedByDescending { it.timestamp }
        
        // Filter for today's moods only
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val startOfNextDay = calendar.timeInMillis
        
        val todaysMoods = allMoods.filter { mood ->
            mood.timestamp >= startOfDay && mood.timestamp < startOfNextDay
        }
        
        // Update mood count badge
        tvMoodCount.text = todaysMoods.size.toString()
        
        if (todaysMoods.isEmpty()) {
            moodRecyclerView.visibility = View.GONE
            layoutEmptyMoods.visibility = View.VISIBLE
        } else {
            moodRecyclerView.visibility = View.VISIBLE
            layoutEmptyMoods.visibility = View.GONE
            moodAdapter.submitList(todaysMoods)
        }
        
        // Also update the chart and stats with latest data
        loadMoodChartData()
        loadMoodStatsData()
    }

    private fun setupMoodChart() {
        // Set up timeline chip group for chart filtering
        chipGroupTimelineMood.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val timeframe = when (checkedIds.first()) {
                    R.id.chipToday -> MoodChartView.Timeframe.TODAY
                    R.id.chipDaily -> MoodChartView.Timeframe.DAILY
                    R.id.chipWeekly -> MoodChartView.Timeframe.WEEKLY
                    R.id.chipMonthly -> MoodChartView.Timeframe.MONTHLY
                    R.id.chipYearly -> MoodChartView.Timeframe.YEARLY
                    else -> MoodChartView.Timeframe.TODAY
                }
                moodChartView.setTimeframe(timeframe)
                loadMoodChartData()
            }
        }
        
        // Set initial timeframe to today
        moodChartView.setTimeframe(MoodChartView.Timeframe.TODAY)
        loadMoodChartData()
    }

    private fun setupMoodStats() {
        // Stats view will be updated when moods are loaded
        loadMoodStatsData()
    }

    private fun loadMoodChartData() {
        val allMoods = repository.getAllMoods()
        moodChartView.setMoodData(allMoods)
    }

    private fun loadMoodStatsData() {
        val allMoods = repository.getAllMoods()
        moodStatsView.setMoodData(allMoods)
    }

    private fun setupNavigation() {
        bottomNav = findViewById(R.id.bottomNav)
        navRail = findViewById(R.id.navRail)
        
        val navigationListener = { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_habits -> {
                    startActivity(Intent(this, HabitsActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_mood -> {
                    // Already on mood, do nothing
                    true
                }
                R.id.menu_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        
        bottomNav?.setOnItemSelectedListener(navigationListener)
        navRail?.setOnItemSelectedListener(navigationListener)
        
        // Set mood as selected
        bottomNav?.selectedItemId = R.id.menu_mood
        navRail?.selectedItemId = R.id.menu_mood
    }
}