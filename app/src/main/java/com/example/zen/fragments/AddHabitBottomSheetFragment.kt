package com.example.zen.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.zen.R
import com.example.zen.models.Habit
import com.example.zen.repo.ZenRepository
import com.example.zen.util.AlarmScheduler
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch

class AddHabitBottomSheetFragment : DialogFragment() {

    private lateinit var repository: ZenRepository
    private var onHabitCreated: (() -> Unit)? = null

    // UI Components
    private lateinit var etEmoji: TextInputEditText
    private lateinit var tilEmoji: TextInputLayout
    private lateinit var etHabitName: TextInputEditText
    private lateinit var tilHabitName: TextInputLayout
    private lateinit var etUnit: TextInputEditText
    private lateinit var tilUnit: TextInputLayout
    private lateinit var etDailyTarget: TextInputEditText
    private lateinit var tilDailyTarget: TextInputLayout
    private lateinit var etDefaultIncrement: TextInputEditText
    private lateinit var tilDefaultIncrement: TextInputLayout
    private lateinit var chipGroupIncrements: ChipGroup
    private lateinit var tilCategory: TextInputLayout
    private lateinit var actCategory: com.google.android.material.textfield.MaterialAutoCompleteTextView
    private lateinit var switchReminders: SwitchMaterial
    private lateinit var llReminderContainer: LinearLayout
    private lateinit var btnAddReminder: MaterialButton
    private lateinit var switchFavorites: SwitchMaterial
    private lateinit var btnSave: MaterialButton
    private lateinit var btnClose: ImageButton

    // Data
    private val reminderTimes = mutableListOf<String>()
    private var editingHabit: Habit? = null
    private val categories = listOf("Health", "Productivity", "Mindfulness")
    private var selectedCategory: String = categories.first()

    // Permission launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            switchReminders.isChecked = false
            Toast.makeText(requireContext(), "Notification permission is required for reminders", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(onHabitCreated: (() -> Unit)? = null, habitToEdit: Habit? = null): AddHabitBottomSheetFragment {
            return AddHabitBottomSheetFragment().apply {
                this.onHabitCreated = onHabitCreated
                this.editingHabit = habitToEdit
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.FullScreenDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_habit, container, false)
    }

    override fun onStart() {
        super.onStart()
        
        // Set dialog window properties
        dialog?.window?.let { window ->
            val params = window.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
            
            // Make status bar and navigation bar transparent
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            // Add margins by adjusting the dialog content
            val decorView = window.decorView
            val marginHorizontal = (16 * resources.displayMetrics.density).toInt() // Convert 16dp to pixels
            val marginVertical = (32 * resources.displayMetrics.density).toInt() // Convert 32dp to pixels
            decorView.setPadding(marginHorizontal, marginVertical, marginHorizontal, marginVertical) // Margins on all sides
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = ZenRepository.getInstance(requireContext())
        
        initViews(view)
        setupListeners()
        setupValidation()
        setupCategoryDropdown()
        setupKeyboardHandling(view)
        
        // Populate form if editing
        editingHabit?.let { populateFormForEdit(it) }
    }
    
    private fun setupKeyboardHandling(view: View) {
        // Find the ScrollView
    val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        
        // Ensure the ScrollView can handle keyboard properly
        scrollView?.let { scroll ->
            scroll.isSmoothScrollingEnabled = true
            
            // Focus listeners for input fields to scroll when keyboard appears
            val inputFields = listOf(etEmoji, etHabitName, etDailyTarget, etDefaultIncrement)
            inputFields.forEach { editText ->
                editText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        scroll.post {
                            scroll.smoothScrollTo(0, editText.bottom)
                        }
                    }
                }
            }
        }
    }
    
    private fun initViews(view: View) {
        etEmoji = view.findViewById(R.id.etEmoji)
        tilEmoji = view.findViewById(R.id.tilEmoji)
        etHabitName = view.findViewById(R.id.etHabitName)
        tilHabitName = view.findViewById(R.id.tilHabitName)
        etUnit = view.findViewById(R.id.etUnit)
        tilUnit = view.findViewById(R.id.tilUnit)
        etDailyTarget = view.findViewById(R.id.etDailyTarget)
        tilDailyTarget = view.findViewById(R.id.tilDailyTarget)
        etDefaultIncrement = view.findViewById(R.id.etDefaultIncrement)
        tilDefaultIncrement = view.findViewById(R.id.tilDefaultIncrement)
        chipGroupIncrements = view.findViewById(R.id.chipGroupIncrements)
        tilCategory = view.findViewById(R.id.tilCategory)
        actCategory = view.findViewById(R.id.actCategory)
        switchReminders = view.findViewById(R.id.switchReminders)
        llReminderContainer = view.findViewById(R.id.llReminderContainer)
        btnAddReminder = view.findViewById(R.id.btnAddReminder)
        switchFavorites = view.findViewById(R.id.switchFavorites)
        btnSave = view.findViewById(R.id.btnSave)
        btnClose = view.findViewById(R.id.btnClose)
        
        // Set up emoji-only input filter
        setupEmojiFilter()
    }
    
    private fun setupEmojiFilter() {
        etEmoji.filters = arrayOf(android.text.InputFilter { source, start, end, dest, dstart, dend ->
            if (source.isNullOrEmpty()) return@InputFilter null
            
            val input = source.subSequence(start, end).toString()
            
            // If input is empty, allow it (for deletions)
            if (input.isEmpty()) return@InputFilter null
            
            // Check if the input is a valid emoji
            if (isValidEmoji(input)) {
                // If destination already has content, replace it with the new emoji
                if (dest.isNotEmpty()) {
                    return@InputFilter input
                }
                return@InputFilter null // Allow the input
            }
            
            // Reject non-emoji input
            return@InputFilter ""
        })
        
        // Limit to 2 characters max (to accommodate some emojis that are 2 characters)
        etEmoji.filters = etEmoji.filters + android.text.InputFilter.LengthFilter(2)
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        actCategory.setAdapter(adapter)
        actCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories.getOrElse(position) { categories.first() }
            tilCategory.error = null
            tilCategory.isHintEnabled = false // Hide hint after selection
        }
        actCategory.setOnClickListener {
            actCategory.showDropDown()
        }
        actCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                actCategory.showDropDown()
            }
        }
        actCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString()?.trim().orEmpty()
                if (value.isNotEmpty() && categories.any { it.equals(value, ignoreCase = true) }) {
                    selectedCategory = categories.first { it.equals(value, ignoreCase = true) }
                    tilCategory.error = null
                    tilCategory.isHintEnabled = false // Hide hint when text entered
                } else if (value.isEmpty()) {
                    tilCategory.isHintEnabled = true // Show hint when field is empty
                }
            }
        })

        val initialValue = actCategory.text?.toString()?.trim().orEmpty()
        if (initialValue.isNotEmpty()) {
            val match = categories.firstOrNull { it.equals(initialValue, ignoreCase = true) }
            if (match != null) {
                selectedCategory = match
                actCategory.setText(match, false)
                tilCategory.isHintEnabled = false // Hide hint for initial value
            } else {
                actCategory.setText("", false)
                selectedCategory = categories.first()
                tilCategory.isHintEnabled = true // Show hint when empty
            }
        }
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { 
            if (hasUnsavedChanges()) {
                showDiscardChangesDialog()
            } else {
                dismiss()
            }
        }
        
        btnSave.setOnClickListener { saveHabit() }
        
        switchReminders.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Check notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnCheckedChangeListener
                    }
                }
                llReminderContainer.visibility = View.VISIBLE
            } else {
                llReminderContainer.visibility = View.GONE
            }
        }
        
        btnAddReminder.setOnClickListener { showTimePicker() }
        
        etEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        })
        
        // Update increment chip suggestions when unit changes
        etUnit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val unit = s?.toString() ?: ""
                if (unit.isNotEmpty()) {
                    updateIncrementChips(unit)
                }
            }
        })
    }
    
    private fun setupValidation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { 
                // Clear errors when user starts typing
                clearFieldErrors()
            }
        }
        
        etHabitName.addTextChangedListener(textWatcher)
        etUnit.addTextChangedListener(textWatcher)
        etDailyTarget.addTextChangedListener(textWatcher)
        etDefaultIncrement.addTextChangedListener(textWatcher)
        etEmoji.addTextChangedListener(textWatcher)
        actCategory.addTextChangedListener(textWatcher)
    }
    
    private fun clearFieldErrors() {
        tilEmoji.error = null
        tilHabitName.error = null
        tilUnit.error = null
        tilDailyTarget.error = null
        tilDefaultIncrement.error = null
        tilCategory.error = null
    }
    
    private fun updateIncrementChips(unit: String) {
        chipGroupIncrements.removeAllViews()
        
        val suggestions = when (unit.uppercase()) {
            "ML", "MILLILITERS" -> listOf(100, 250, 500)
            "L", "LITERS" -> listOf(1, 2, 3)
            "MINUTES", "MIN", "MINS" -> listOf(5, 10, 15)
            "STEPS" -> listOf(500, 1000, 2000)
            "COUNT", "TIMES" -> listOf(1, 5, 10)
            "PAGES" -> listOf(10, 20, 30)
            "KM", "KILOMETERS" -> listOf(1, 3, 5)
            "HOURS", "HRS" -> listOf(1, 2, 3)
            else -> listOf(1, 5, 10) // Default suggestions
        }
        
        suggestions.forEach { value ->
            val chip = Chip(requireContext())
            chip.text = value.toString()
            chip.isClickable = true
            chip.setOnClickListener {
                etDefaultIncrement.setText(value.toString())
            }
            chipGroupIncrements.addView(chip)
        }
    }
    
    private fun isValidEmoji(text: String): Boolean {
        if (text.isEmpty() || text.length > 2) return false
        
        // Check if the text contains emoji characters
        val codePoints = text.codePoints().toArray()
        
        for (codePoint in codePoints) {
            when {
                // Common emoji ranges
                codePoint in 0x1F600..0x1F64F || // Emoticons
                codePoint in 0x1F300..0x1F5FF || // Misc Symbols and Pictographs
                codePoint in 0x1F680..0x1F6FF || // Transport and Map
                codePoint in 0x1F700..0x1F77F || // Alchemical Symbols
                codePoint in 0x2600..0x26FF ||   // Misc symbols
                codePoint in 0x2700..0x27BF ||   // Dingbats
                codePoint in 0xFE00..0xFE0F ||   // Variation Selectors
                codePoint in 0x1F900..0x1F9FF || // Supplemental Symbols and Pictographs
                codePoint in 0x1F1E6..0x1F1FF || // Regional Indicator Symbols (flags)
                codePoint in 0x2000..0x206F ||   // General Punctuation (for zero-width joiners)
                codePoint == 0x200D              // Zero Width Joiner (for compound emojis)
                -> continue
                else -> return false
            }
        }
        return true
    }
    
    private fun validateForm(): Boolean {
        var isValid = true

        val emoji = etEmoji.text?.toString() ?: ""
        if (emoji.isEmpty()) {
            tilEmoji.error = "Please enter an emoji for the habit icon"
            isValid = false
        } else if (!isValidEmoji(emoji)) {
            tilEmoji.error = "Please enter a single emoji for the habit icon"
            isValid = false
        } else {
            tilEmoji.error = null
        }

        val name = etHabitName.text?.toString()?.trim() ?: ""
        when {
            name.isEmpty() -> {
                tilHabitName.error = "Please enter a habit name"
                isValid = false
            }
            name.length > 40 -> {
                tilHabitName.error = "Maximum 40 characters"
                isValid = false
            }
            else -> tilHabitName.error = null
        }

        val unit = etUnit.text?.toString()?.trim() ?: ""
        when {
            unit.isEmpty() -> {
                tilUnit.error = "Please enter a unit"
                isValid = false
            }
            unit.length > 20 -> {
                tilUnit.error = "Maximum 20 characters"
                isValid = false
            }
            else -> tilUnit.error = null
        }

        val categoryValue = actCategory.text?.toString()?.trim().orEmpty()
        when {
            categoryValue.isEmpty() -> {
                tilCategory.error = "Please select a category"
                isValid = false
            }
            categories.none { it.equals(categoryValue, ignoreCase = true) } -> {
                tilCategory.error = "Unknown category"
                isValid = false
            }
            else -> {
                selectedCategory = categories.first { it.equals(categoryValue, ignoreCase = true) }
                tilCategory.error = null
            }
        }

        val target = etDailyTarget.text?.toString()?.toIntOrNull()
        when {
            target == null -> {
                tilDailyTarget.error = "Please enter a valid number"
                isValid = false
            }
            target <= 0 -> {
                tilDailyTarget.error = "Daily target must be greater than 0"
                isValid = false
            }
            else -> tilDailyTarget.error = null
        }

        val increment = etDefaultIncrement.text?.toString()?.toIntOrNull()
        when {
            increment == null -> {
                tilDefaultIncrement.error = "Please enter a valid number"
                isValid = false
            }
            increment <= 0 -> {
                tilDefaultIncrement.error = "Default increment must be greater than 0"
                isValid = false
            }
            else -> tilDefaultIncrement.error = null
        }

        return isValid
    }
    
    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(9)
            .setMinute(0)
            .setTitleText("Select reminder time")
            .build()
            
        picker.addOnPositiveButtonClickListener {
            val timeString = String.format("%02d:%02d", picker.hour, picker.minute)
            if (!reminderTimes.contains(timeString)) {
                reminderTimes.add(timeString)
                addReminderChip(timeString)
            } else {
                Toast.makeText(requireContext(), "This reminder time already exists", Toast.LENGTH_SHORT).show()
            }
        }
        
        picker.show(parentFragmentManager, "time_picker")
    }
    
    private fun addReminderChip(time: String) {
        val chip = Chip(requireContext())
        chip.text = time
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            reminderTimes.remove(time)
            llReminderContainer.removeView(chip)
        }
        llReminderContainer.addView(chip, llReminderContainer.childCount - 1) // Add before the "Add" button
    }
    
    private fun clearReminderChips() {
        // Remove all chip views except the "Add" button (which should be the last child)
        val childCount = llReminderContainer.childCount
        for (i in childCount - 2 downTo 0) { // Skip the last child (Add button)
            val child = llReminderContainer.getChildAt(i)
            if (child is Chip) {
                llReminderContainer.removeView(child)
            }
        }
    }
    
    private fun saveHabit() {
        // Validate form first
        if (!validateForm()) {
            return
        }
        
        val emoji = etEmoji.text?.toString()?.trim() ?: ""
        val name = etHabitName.text?.toString()?.trim() ?: ""
        val unit = etUnit.text?.toString()?.trim()?.uppercase() ?: "COUNT"
        val target = etDailyTarget.text?.toString()?.toIntOrNull() ?: 0
        val increment = etDefaultIncrement.text?.toString()?.toIntOrNull() ?: 0
        val categoryText = actCategory.text?.toString()?.trim().orEmpty()
        val category = categories.firstOrNull { it.equals(categoryText, ignoreCase = true) } ?: selectedCategory
        selectedCategory = category
        
        // Check if increment > target
        if (increment > target) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Default increment is greater than daily target. Continue?")
                .setPositiveButton("Accept") { _, _ -> proceedWithSave(emoji, name, unit, target, increment, category) }
                .setNegativeButton("Adjust", null)
                .show()
        } else {
            proceedWithSave(emoji, name, unit, target, increment, category)
        }
    }
    
    private fun proceedWithSave(emoji: String, name: String, unit: String, target: Int, increment: Int, category: String) {
        val habit = if (editingHabit != null) {
            // Update existing habit
            editingHabit!!.copy(
                title = name,
                emoji = emoji,
                unit = unit,
                targetPerDay = target,
                defaultIncrement = increment,
                isStarred = switchFavorites.isChecked,
                reminderTimes = reminderTimes.toList(),
                category = category
            )
        } else {
            // Create new habit
            Habit(
                id = "habit_${System.currentTimeMillis()}",
                title = name,
                emoji = emoji,
                unit = unit,
                targetPerDay = target,
                defaultIncrement = increment,
                category = category,
                isBuiltIn = false,
                isStarred = switchFavorites.isChecked,
                reminderTimes = reminderTimes.toList(),
                enabled = true,
                createdAt = System.currentTimeMillis()
            )
        }
        
        lifecycleScope.launch {
            try {
                if (editingHabit != null) {
                    repository.updateHabit(habit)
                } else {
                    repository.addHabit(habit)
                }
                
                // Schedule reminders if enabled and permission granted
                if (switchReminders.isChecked && reminderTimes.isNotEmpty()) {
                    // Check notification permission again before scheduling
                    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true // No permission needed on older versions
                    }
                    
                    if (hasNotificationPermission) {
                        AlarmScheduler.scheduleForHabit(requireContext(), habit.id, reminderTimes)
                    } else {
                        Toast.makeText(requireContext(), "Notification permission not granted. Reminders will not work.", Toast.LENGTH_LONG).show()
                    }
                }
                
                val message = if (editingHabit != null) "Habit updated" else "Habit created"
                activity?.let { activity ->
                    Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            lifecycleScope.launch {
                                if (editingHabit != null) {
                                    repository.updateHabit(editingHabit!!)
                                    // Reschedule alarms for the original habit
                                    AlarmScheduler.scheduleForHabit(requireContext(), editingHabit!!.id, editingHabit!!.reminderTimes)
                                } else {
                                    repository.deleteHabit(habit.id)
                                    // Cancel all scheduled alarms for this habit
                                    habit.reminderTimes.forEach { timeStr ->
                                        AlarmScheduler.cancelScheduledAlarm(requireContext(), habit.id, timeStr)
                                    }
                                }
                            }
                        }
                        .show()
                }
                
                onHabitCreated?.invoke()
                dismiss()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating habit: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun hasUnsavedChanges(): Boolean {
        return etEmoji.text?.isNotEmpty() == true ||
               etHabitName.text?.isNotEmpty() == true ||
               etDailyTarget.text?.isNotEmpty() == true ||
               etDefaultIncrement.text?.isNotEmpty() == true ||
               reminderTimes.isNotEmpty()
    }
    
    private fun populateFormForEdit(habit: Habit) {
        // Set title
        view?.findViewById<TextView>(R.id.tvTitle)?.text = "Edit Habit"
        btnSave.text = "Update Habit"
        
        // Populate emoji
        etEmoji.setText(habit.emoji)
        
        selectedCategory = categories.firstOrNull { it.equals(habit.category, ignoreCase = true) } ?: categories.first()
        actCategory.setText(selectedCategory, false)
        tilCategory.error = null
        tilCategory.isHintEnabled = false // Hide hint when category is already set
        
        // Populate habit name
        etHabitName.setText(habit.title)
        
        // Populate unit
        etUnit.setText(habit.unit)
        
        // Populate daily target
        etDailyTarget.setText(habit.targetPerDay.toString())
        
        // Populate default increment
        etDefaultIncrement.setText(habit.defaultIncrement.toString())
        
        // Populate reminders
        reminderTimes.clear()
        clearReminderChips() // Clear existing chips from UI
        reminderTimes.addAll(habit.reminderTimes)
        switchReminders.isChecked = habit.reminderTimes.isNotEmpty()
        llReminderContainer.visibility = if (habit.reminderTimes.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Add reminder chips for existing reminder times
        habit.reminderTimes.forEach { timeString ->
            addReminderChip(timeString)
        }
        
        // Populate favorites
        switchFavorites.isChecked = habit.isStarred
        
        // Update increment chips for the selected unit
        updateIncrementChips(habit.unit)
    }
    
    private fun showDiscardChangesDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Discard changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Discard") { _, _ -> dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}