package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.zen.R
import com.example.zen.models.UserProfile
import com.example.zen.repo.ZenRepository
import com.example.zen.util.DateUtils
import com.example.zen.views.ProfileStatsView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import java.util.concurrent.TimeUnit

class ProfileActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null
    private var navRail: NavigationRailView? = null
    private lateinit var repo: ZenRepository

    // Views
    private lateinit var ivAvatarEmoji: TextView
    private lateinit var tvName: TextView
    private lateinit var tvAgeGender: TextView
    private lateinit var tvDaysSinceSignup: TextView
    private lateinit var profileStatsView: ProfileStatsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        repo = ZenRepository.getInstance(this)

        bindViews()
        bindButtons()
        renderProfile()
        renderStats()
        setupNavigation()
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
                    startActivity(Intent(this, MoodActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_profile -> {
                    // Already on profile, do nothing
                    true
                }
                else -> false
            }
        }
        
        bottomNav?.setOnItemSelectedListener(navigationListener)
        navRail?.setOnItemSelectedListener(navigationListener)
        
        // Set profile as selected
        bottomNav?.selectedItemId = R.id.menu_profile
        navRail?.selectedItemId = R.id.menu_profile
    }

    private fun bindViews() {
        ivAvatarEmoji = findViewById(R.id.ivAvatarEmoji)
        tvName = findViewById(R.id.tvName)
        tvAgeGender = findViewById(R.id.tvAgeGender)
        tvDaysSinceSignup = findViewById(R.id.tvDaysSinceSignup)
        profileStatsView = findViewById(R.id.profileStatsView)
    }

    private fun bindButtons() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun renderProfile() {
        val profile: UserProfile? = repo.getUserProfile()
        if (profile == null) {
            tvName.text = getString(R.string.profile_title)
            tvAgeGender.text = ""
            ivAvatarEmoji.text = "🫣"
            tvDaysSinceSignup.text = "🎉 Welcome to Zen!"
        } else {
            ivAvatarEmoji.text = profile.avatarEmoji
            tvName.text = profile.name
            tvAgeGender.text = "${profile.age}, ${profile.gender}"
            
            // Calculate days since signup
            val daysSinceSignup = TimeUnit.MILLISECONDS.toDays(
                System.currentTimeMillis() - profile.createdAt
            ).toInt()
            
            tvDaysSinceSignup.text = when {
                daysSinceSignup == 0 -> "🎉 Welcome to Zen!"
                daysSinceSignup == 1 -> "🎉 Day 1 on Zen"
                daysSinceSignup < 7 -> "🎉 Day $daysSinceSignup on Zen"
                daysSinceSignup < 30 -> "✨ ${daysSinceSignup / 7} weeks on Zen"
                daysSinceSignup < 365 -> "🌟 ${daysSinceSignup / 30} months on Zen"
                else -> "🏆 ${daysSinceSignup / 365} years on Zen!"
            }
        }
    }
    
    private fun renderStats() {
        val habits = repo.getAllHabits()
        val longestStreak = calculateLongestStreak()

        val statsList = listOf(
            ProfileStatsView.StatItem(
                label = "Total Habits",
                value = habits.size,
                maxValue = 20,
                emoji = "🎯",
                color = android.graphics.Color.parseColor("#1A4C8B")
            ),
            ProfileStatsView.StatItem(
                label = "Best Streak",
                value = longestStreak,
                maxValue = 30,
                emoji = "🔥",
                color = android.graphics.Color.parseColor("#FF6B35")
            )
        )
        
        profileStatsView.setStats(statsList)
    }
    
    private fun calculateLongestStreak(): Int {
        val habits = repo.getAllHabits()
        var maxStreak = 0
        
        habits.forEach { habit ->
            val ticks = repo.getTicksForHabit(habit.id)
            val dates = ticks.map { it.date }.distinct().sorted()
            
            var currentStreak = 0
            var lastDate: String? = null
            
            dates.forEach { date ->
                if (lastDate == null) {
                    currentStreak = 1
                } else {
                    val daysDiff = DateUtils.daysBetween(lastDate!!, date)
                    if (daysDiff == 1) {
                        currentStreak++
                    } else {
                        currentStreak = 1
                    }
                }
                maxStreak = maxOf(maxStreak, currentStreak)
                lastDate = date
            }
        }
        
        return maxStreak
    }
    

    override fun onResume() {
        super.onResume()
        // Refresh in case profile was edited
        renderProfile()
        renderStats()
    }
}
