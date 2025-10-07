package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zen.R
import com.example.zen.models.Habit
import com.example.zen.repo.ZenRepository
import com.example.zen.util.DateUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigationrail.NavigationRailView

class HabitsActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null
    private var navRail: NavigationRailView? = null
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: HabitsAdapter
    private lateinit var repo: ZenRepository
    private lateinit var chipCategories: ChipGroup
    private lateinit var fab: FloatingActionButton
    private var currentSortOrder = SortOrder.BY_NAME
    
    enum class SortOrder {
        BY_NAME, BY_PROGRESS, BY_STREAK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)
        repo = ZenRepository.getInstance(this)
        setupNavigation()
        initFilters()
        initList()
        initFab()
        initStats()
        initSortButton()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list to show any updates made in HabitDetailActivity
        refreshList()
        updateStats()
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
                    // Already on habits, do nothing
                    true
                }
                R.id.menu_mood -> {
                    startActivity(Intent(this, MoodActivity::class.java))
                    finish()
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
        
        // Set habits as selected
        bottomNav?.selectedItemId = R.id.menu_habits
        navRail?.selectedItemId = R.id.menu_habits
    }

    private fun initList() {
        recycler = findViewById(R.id.rvHabits)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = HabitsAdapter(
            onQuickAdd = { habit ->
                // Add the tick to repository
                repo.addTick(habit.id, habit.defaultIncrement)
                
                // Refresh to show the updated count
                // Using post to ensure the repository update is completed first
                recycler.post {
                    refreshList()
                }
                
                // Show user feedback
                val message = "Added ${habit.defaultIncrement} to ${habit.title}"
                val coordinatorLayout = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.coordinatorLayout)
                com.google.android.material.snackbar.Snackbar.make(coordinatorLayout, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            },
            onCardClick = { habit ->
                val intent = HabitDetailActivity.newIntent(this, habit.id)
                startActivity(intent)
            },
            repository = repo
        )
        recycler.adapter = adapter
        refreshList()
    }

    private fun initFilters() {
        chipCategories = findViewById(R.id.chipGroupCategories)
        chipCategories.setOnCheckedStateChangeListener { _, _ ->
            refreshList()
        }
        // Ensure one is checked by default (All)
        if (chipCategories.checkedChipId == -1 && chipCategories.childCount > 0) {
            chipCategories.check(chipCategories.getChildAt(0).id)
        }
        
        // Wire search functionality
        val searchInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchHabits)
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                refreshList()
            }
        })
    }
    
    private fun initStats() {
        updateStats()
    }
    
    private fun updateStats() {
        val allHabits = repo.getAllHabits()
        val today = DateUtils.nowDateString()
        val todayTicks = repo.getTicksForDate(today)
        
        // Count completed habits (reached target)
        var completedToday = 0
        for (habit in allHabits) {
            val tick = todayTicks.firstOrNull { it.habitId == habit.id }
            val progress = tick?.amount ?: 0
            if (progress >= habit.targetPerDay) {
                completedToday++
            }
        }
        
        findViewById<TextView>(R.id.tvTodayCompleted)?.text = completedToday.toString()
        findViewById<TextView>(R.id.tvActiveHabits)?.text = allHabits.size.toString()
        findViewById<TextView>(R.id.tvHabitCount)?.text = "${allHabits.size} active habits"
    }
    
    private fun initSortButton() {
        val btnSort = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSort)
        btnSort?.setOnClickListener {
            showSortMenu(it)
        }
    }
    
    private fun showSortMenu(anchor: View) {
        val popup = android.widget.PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_sort_habits, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_by_name -> {
                    currentSortOrder = SortOrder.BY_NAME
                    refreshList()
                    true
                }
                R.id.sort_by_progress -> {
                    currentSortOrder = SortOrder.BY_PROGRESS
                    refreshList()
                    true
                }
                R.id.sort_by_streak -> {
                    currentSortOrder = SortOrder.BY_STREAK
                    refreshList()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun initFab() {
        fab = findViewById(R.id.fabAddHabit)
        fab.setOnClickListener {
            openAddHabitModal()
        }
    }
    
    private fun openAddHabitModal() {
        val addHabitFragment = com.example.zen.fragments.AddHabitBottomSheetFragment.newInstance(
            onHabitCreated = {
                refreshList() // Refresh the list when a new habit is created
            }
        )
        addHabitFragment.show(supportFragmentManager, "add_habit")
    }

    private fun refreshList() {
        val all = repo.getAllHabits()
        val today = DateUtils.nowDateString()
        val todayTicks = repo.getTicksForDate(today)
        val progressByHabit = all.associate { h ->
            val tick = todayTicks.firstOrNull { it.habitId == h.id }
            h.id to (tick?.amount ?: 0)
        }
        
        // Get search text
        val searchText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearchHabits)?.text?.toString()?.trim() ?: ""
        
        // Filter by search text
        val searchFiltered = if (searchText.isNotEmpty()) {
            all.filter { 
                it.title.contains(searchText, ignoreCase = true) 
            }
        } else {
            all
        }
        
        // Filter by category selection
        val checkedId = if (::chipCategories.isInitialized) chipCategories.checkedChipId else -1

        val categoryFiltered = when (checkedId) {
            R.id.chipAll -> searchFiltered
            R.id.chipHealth -> searchFiltered.filter { it.category.equals("Health", ignoreCase = true) }
            R.id.chipProductivity -> searchFiltered.filter { it.category.equals("Productivity", ignoreCase = true) }
            R.id.chipMindfulness -> searchFiltered.filter { it.category.equals("Mindfulness", ignoreCase = true) }
            R.id.chipFavorites -> searchFiltered.filter { it.isStarred }
            else -> searchFiltered
        }
        // Apply sorting
        val sorted = when (currentSortOrder) {
            SortOrder.BY_NAME -> categoryFiltered.sortedBy { it.title.lowercase() }
            SortOrder.BY_PROGRESS -> categoryFiltered.sortedByDescending { h ->
                val progress = progressByHabit[h.id] ?: 0
                if (h.targetPerDay > 0) (progress * 100) / h.targetPerDay else 0
            }
            SortOrder.BY_STREAK -> categoryFiltered.sortedByDescending { h ->
                calculateStreak(h.id)
            }
        }
        
        adapter.submit(sorted, progressByHabit)
        updateStats()
    }
    
    private fun calculateStreak(habitId: String): Int {
        val allTicks = repo.getTicksForHabit(habitId)
        val habit = repo.getAllHabits().find { it.id == habitId } ?: return 0
        
        if (allTicks.isEmpty()) return 0
        
        val ticksByDate = allTicks.groupBy { tick -> tick.date }.mapValues { entry ->
            entry.value.sumOf { tick -> tick.amount }
        }
        
        var streak = 0
        var currentDate = java.time.LocalDate.now()
        
        while (true) {
            val dateStr = currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val tickAmount = ticksByDate[dateStr] ?: 0
            if (tickAmount >= habit.targetPerDay) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }

    private class HabitsAdapter(
        val onQuickAdd: (Habit) -> Unit,
        val onCardClick: (Habit) -> Unit,
        val repository: ZenRepository
    ) : RecyclerView.Adapter<HabitsAdapter.VH>() {
    private val items = mutableListOf<Habit>()
        private var progress: Map<String, Int> = emptyMap()

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle: TextView = v.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = v.findViewById(R.id.tvSubtitle)
            val ivEmoji: TextView = v.findViewById(R.id.ivEmoji)
            val btnPlus: com.google.android.material.button.MaterialButton = v.findViewById(R.id.btnPlus)
            val ivStar: ImageView = v.findViewById(R.id.ivStar)
            val progressBar: android.widget.ProgressBar = v.findViewById(R.id.progressBar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val h = items[position]
            holder.tvTitle.text = h.title
            holder.ivEmoji.text = h.emoji
            val value = progress[h.id] ?: 0
            holder.tvSubtitle.text = when (h.unit) {
                "ML" -> "${value} mL / ${h.targetPerDay} mL"
                "MINUTES" -> "${value} / ${h.targetPerDay} min"
                "STEPS" -> "${value} / ${h.targetPerDay} steps"
                else -> "${value} / ${h.targetPerDay}"
            }
            val pct = if (h.targetPerDay > 0) (value * 100 / h.targetPerDay).coerceIn(0, 100) else 0
            holder.progressBar.progress = pct
            holder.ivStar.setImageResource(if (h.isStarred) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
            holder.itemView.setOnClickListener { onCardClick(h) }
            holder.btnPlus.setOnClickListener { onQuickAdd(h) }
            holder.ivStar.setOnClickListener {
                val updated = h.copy(isStarred = !h.isStarred)
                repository.updateHabit(updated)
                items[holder.bindingAdapterPosition] = updated
                notifyItemChanged(holder.bindingAdapterPosition)
            }
        }

        override fun getItemCount(): Int = items.size

        fun submit(list: List<Habit>, progressMap: Map<String, Int>) {
            val oldItems = items.toList()
            val oldProgress = progress
            progress = progressMap
            
            // compute minimal change set
            val diff = androidx.recyclerview.widget.DiffUtil.calculateDiff(object : androidx.recyclerview.widget.DiffUtil.Callback() {
                override fun getOldListSize() = oldItems.size
                override fun getNewListSize() = list.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldItems[oldItemPosition].id == list[newItemPosition].id
                }
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val o = oldItems[oldItemPosition]
                    val n = list[newItemPosition]
                    val oldProgressValue = oldProgress[o.id] ?: 0
                    val newProgressValue = progressMap[n.id] ?: 0
                    return o == n && oldProgressValue == newProgressValue
                }
            })
            items.clear()
            items.addAll(list)
            diff.dispatchUpdatesTo(this)
        }
    }
}
