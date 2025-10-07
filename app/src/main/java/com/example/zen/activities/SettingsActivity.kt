package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.zen.R
import com.example.zen.models.AppSettings
import com.example.zen.repo.ZenRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    private lateinit var repo: ZenRepository

    private lateinit var switchStepSensor: MaterialSwitch

    private lateinit var etMoodStart: TextInputEditText
    private lateinit var etMoodInterval: TextInputEditText
    private lateinit var etMoodEnd: TextInputEditText
    private lateinit var btnSaveMood: com.google.android.material.button.MaterialButton

    private lateinit var etHydrationStart: TextInputEditText
    private lateinit var etHydrationInterval: TextInputEditText
    private lateinit var etHydrationEnd: TextInputEditText
    private lateinit var btnSaveHydration: com.google.android.material.button.MaterialButton

    private lateinit var spinnerWidgetHabit: MaterialAutoCompleteTextView
    private lateinit var btnSaveWidget: com.google.android.material.button.MaterialButton

    private lateinit var btnTestAlarm: com.google.android.material.button.MaterialButton
    private lateinit var btnDeleteData: com.google.android.material.button.MaterialButton

    private var bottomNav: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        repo = ZenRepository.getInstance(this)

        bindViews()
        render()
        bindActions()
        setupToolbar()
        setupBottomNav()
    }

    private fun bindViews() {
        switchStepSensor = findViewById<MaterialSwitch>(R.id.switchStepSensor)

        etMoodStart = findViewById(R.id.etMoodStart)
        etMoodInterval = findViewById(R.id.etMoodInterval)
        etMoodEnd = findViewById(R.id.etMoodEnd)
        btnSaveMood = findViewById(R.id.btnSaveMoodSchedule)

        etHydrationStart = findViewById(R.id.etHydrationStart)
        etHydrationInterval = findViewById(R.id.etHydrationInterval)
        etHydrationEnd = findViewById(R.id.etHydrationEnd)
        btnSaveHydration = findViewById(R.id.btnSaveHydrationSchedule)

        spinnerWidgetHabit = findViewById(R.id.spinnerWidgetHabit)
        btnSaveWidget = findViewById(R.id.btnSaveWidget)

        btnTestAlarm = findViewById(R.id.btnTestAlarm)
        btnDeleteData = findViewById(R.id.btnDeleteData)
        bottomNav = findViewById(R.id.bottomNav)
    }

    private fun render() {
        val s = repo.getSettings()
        switchStepSensor.isChecked = s.stepSensorEnabled

        etMoodStart.setText(s.moodStartTime)
        etMoodEnd.setText(s.moodEndTime)
        etMoodInterval.setText(s.moodIntervalMinutes.toString())

        etHydrationStart.setText(s.hydrationStartTime)
        etHydrationEnd.setText(s.hydrationEndTime)
        etHydrationInterval.setText(s.hydrationIntervalMinutes.toString())

        setMoodScheduleEnabled(s.notificationsMood)
        setHydrationScheduleEnabled(s.notificationsHydration)
        
        // Auto-start step service if enabled
        if (s.stepSensorEnabled) {
            autoStartStepServiceIfNeeded()
        }
        
        // Set up widget habit dropdown
        setupWidgetHabitDropdown(s)
    }

    private fun setupWidgetHabitDropdown(settings: com.example.zen.models.AppSettings) {
        val habits = repo.getAllHabits()
        val habitOptions = mutableListOf<String>()
        val habitIds = mutableListOf<String>()
        
        // Add "None" option
        habitOptions.add("None")
        habitIds.add("")
        
        // Add all habits
        habits.forEach { habit ->
            habitOptions.add("${habit.emoji} ${habit.title}")
            habitIds.add(habit.id)
        }
        
        // If no habits available, add a message
        if (habits.isEmpty()) {
            habitOptions.add("No habits available")
            habitIds.add("")
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, habitOptions)
        spinnerWidgetHabit.setAdapter(adapter)
        
        // Set current selection
        val currentIndex = habitIds.indexOf(settings.widgetSelectedHabitId)
        if (currentIndex >= 0) {
            spinnerWidgetHabit.setText(habitOptions[currentIndex], false)
        } else {
            spinnerWidgetHabit.setText(habitOptions[0], false) // Default to "None"
        }
        
        // Make dropdown show on click
        spinnerWidgetHabit.setOnClickListener {
            spinnerWidgetHabit.showDropDown()
        }
        
        // Store the mapping for later use
        spinnerWidgetHabit.tag = habitIds
    }

    private fun bindActions() {
        switchStepSensor.setOnCheckedChangeListener { _, isChecked: Boolean ->
            repo.saveSettings(repo.getSettings().copy(stepSensorEnabled = isChecked))
            if (isChecked) {
                startStepService()
            } else {
                stopStepService()
            }
        }

        etMoodStart.setOnClickListener { pickTime(etMoodStart) }
        etMoodEnd.setOnClickListener { pickTime(etMoodEnd) }

        btnSaveMood.setOnClickListener {
            val cur = repo.getSettings()
            val interval = etMoodInterval.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: cur.moodIntervalMinutes
            repo.saveSettings(cur.copy(
                moodStartTime = etMoodStart.text?.toString() ?: cur.moodStartTime,
                moodEndTime = etMoodEnd.text?.toString() ?: cur.moodEndTime,
                moodIntervalMinutes = interval
            ))
            rescheduleMoodAlarms()
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Mood schedule saved", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }

        etHydrationStart.setOnClickListener { pickTime(etHydrationStart) }
        etHydrationEnd.setOnClickListener { pickTime(etHydrationEnd) }

        btnSaveHydration.setOnClickListener {
            val cur = repo.getSettings()
            val interval = etHydrationInterval.text?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: cur.hydrationIntervalMinutes
            repo.saveSettings(cur.copy(
                hydrationStartTime = etHydrationStart.text?.toString() ?: cur.hydrationStartTime,
                hydrationEndTime = etHydrationEnd.text?.toString() ?: cur.hydrationEndTime,
                hydrationIntervalMinutes = interval
            ))
            rescheduleHydrationAlarms()
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Hydration schedule saved", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }

        btnSaveWidget.setOnClickListener {
            val selectedText = spinnerWidgetHabit.text.toString()
            @Suppress("UNCHECKED_CAST")
            val habitIds = spinnerWidgetHabit.tag as? List<String> ?: emptyList()
            val habitOptions = mutableListOf<String>()
            
            // Recreate options list to find selected index (must match the setup method)
            habitOptions.add("None")
            val habits = repo.getAllHabits()
            habits.forEach { habit ->
                habitOptions.add("${habit.emoji} ${habit.title}")
            }
            if (habits.isEmpty()) {
                habitOptions.add("No habits available")
            }
            
            val selectedIndex = habitOptions.indexOf(selectedText)
            val selectedHabitId = if (selectedIndex >= 0 && selectedIndex < habitIds.size) {
                habitIds[selectedIndex]
            } else {
                ""
            }
            
            val cur = repo.getSettings()
            repo.saveSettings(cur.copy(widgetSelectedHabitId = selectedHabitId))
            
            // Update all widgets
            com.example.zen.widgets.ZenWidgetProvider.WidgetUpdateHelper.updateAllWidgets(this)
            
            val message = if (selectedHabitId.isEmpty()) {
                "Widget will show overall progress only"
            } else {
                val habit = habits.find { it.id == selectedHabitId }
                "Widget will show progress for: ${habit?.title ?: selectedText}"
            }
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }

        btnTestAlarm.setOnClickListener { 
            testAlarm()
        }
        btnDeleteData.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_data_title)
                .setMessage(R.string.delete_data_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    repo.resetAll()
                    Toast.makeText(
                        this,
                        getString(R.string.delete_data_success),
                        Toast.LENGTH_LONG
                    ).show()

                    val restartIntent = Intent(this, SignupActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(restartIntent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupBottomNav() {
        bottomNav?.apply {
            // Pre-select without triggering navigation
            selectedItemId = R.id.menu_profile

            setOnItemSelectedListener { item ->
                if (item.itemId == selectedItemId) return@setOnItemSelectedListener true
                when (item.itemId) {
                    R.id.menu_home -> {
                        startActivity(Intent(this@SettingsActivity, HomeActivity::class.java))
                        finish()
                        true
                    }
                    R.id.menu_habits -> {
                        startActivity(Intent(this@SettingsActivity, HabitsActivity::class.java))
                        finish()
                        true
                    }
                    R.id.menu_mood -> {
                        startActivity(Intent(this@SettingsActivity, MoodActivity::class.java))
                        finish()
                        true
                    }
                    R.id.menu_profile -> {
                        startActivity(Intent(this@SettingsActivity, ProfileActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun pickTime(target: TextInputEditText) {
        val parts = (target.text?.toString() ?: "09:00").split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val dialog = android.app.TimePickerDialog(this, { _, h, m ->
            val v = String.format("%02d:%02d", h, m)
            target.setText(v)
        }, hour, minute, true)
        dialog.show()
    }

    private fun setMoodScheduleEnabled(enabled: Boolean) {
        val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMoodSchedule)
        card.alpha = if (enabled) 1f else 0.5f
        fun setEnabledRecursively(v: android.view.View, e: Boolean) {
            v.isEnabled = e
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) setEnabledRecursively(v.getChildAt(i), e)
            }
        }
        setEnabledRecursively(card, enabled)
    }

    private fun setHydrationScheduleEnabled(enabled: Boolean) {
        val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardHydrationSchedule)
        card.alpha = if (enabled) 1f else 0.5f
        fun setEnabledRecursively(v: android.view.View, e: Boolean) {
            v.isEnabled = e
            if (v is android.view.ViewGroup) {
                for (i in 0 until v.childCount) setEnabledRecursively(v.getChildAt(i), e)
            }
        }
        setEnabledRecursively(card, enabled)
    }

    private fun rescheduleMoodAlarms() {
        android.util.Log.d("SettingsActivity", "rescheduleMoodAlarms called")
        
        val s = repo.getSettings()
        if (!s.notificationsAll || !s.notificationsMood) {
            android.util.Log.d("SettingsActivity", "Mood notifications disabled - notificationsAll: ${s.notificationsAll}, notificationsMood: ${s.notificationsMood}")
            return
        }
        val start = java.time.LocalTime.parse(s.moodStartTime)
        val end = java.time.LocalTime.parse(s.moodEndTime)
        val interval = s.moodIntervalMinutes.coerceAtLeast(1)
        val times = generateAlarmTimes(start, end, interval)
        
        android.util.Log.d("SettingsActivity", "Generated ${times.size} mood alarm times: $times")
        
        com.example.zen.util.AlarmScheduler.scheduleForHabit(this, "mood", times)
    }

    private fun rescheduleHydrationAlarms() {
        android.util.Log.d("SettingsActivity", "rescheduleHydrationAlarms called")
        
        val s = repo.getSettings()
        if (!s.notificationsAll || !s.notificationsHydration) {
            android.util.Log.d("SettingsActivity", "Hydration notifications disabled - notificationsAll: ${s.notificationsAll}, notificationsHydration: ${s.notificationsHydration}")
            return
        }
        val start = java.time.LocalTime.parse(s.hydrationStartTime)
        val end = java.time.LocalTime.parse(s.hydrationEndTime)
        val interval = s.hydrationIntervalMinutes.coerceAtLeast(1)
        val times = generateAlarmTimes(start, end, interval)
        
        android.util.Log.d("SettingsActivity", "Generated ${times.size} hydration alarm times: $times")
        
        com.example.zen.util.AlarmScheduler.scheduleForHabit(this, "hydration", times)
    }

    private fun generateAlarmTimes(start: java.time.LocalTime, end: java.time.LocalTime, interval: Int): List<String> {
        val times = mutableListOf<String>()
        var t = start
        
        // Generate all alarms based on user's interval - no limits
        while (!t.isAfter(end)) {
            times.add(t.toString().substring(0, 5))
            t = t.plusMinutes(interval.toLong())
        }
        
        return times
    }

    override fun onPause() {
        super.onPause()
        // Auto-persist current values so they remain when returning
        val cur = repo.getSettings()
        val moodInterval = etMoodInterval.text?.toString()?.toIntOrNull() ?: cur.moodIntervalMinutes
        val hydrationInterval = etHydrationInterval.text?.toString()?.toIntOrNull() ?: cur.hydrationIntervalMinutes
        val updated = cur.copy(
            moodStartTime = etMoodStart.text?.toString() ?: cur.moodStartTime,
            moodEndTime = etMoodEnd.text?.toString() ?: cur.moodEndTime,
            moodIntervalMinutes = moodInterval.coerceAtLeast(1),
            hydrationStartTime = etHydrationStart.text?.toString() ?: cur.hydrationStartTime,
            hydrationEndTime = etHydrationEnd.text?.toString() ?: cur.hydrationEndTime,
            hydrationIntervalMinutes = hydrationInterval.coerceAtLeast(1)
        )
        if (updated != cur) {
            repo.saveSettings(updated)
            rescheduleMoodAlarms()
            rescheduleHydrationAlarms()
        }
    }


    private fun testAlarm() {
        android.util.Log.d("SettingsActivity", "Test alarm button pressed")
        
        // Schedule an alarm for 1 minute from now
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MINUTE, 1)
        val timeStr = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
        
        android.util.Log.d("SettingsActivity", "Scheduling test alarm for: $timeStr")
        
        com.example.zen.util.AlarmScheduler.scheduleExactDailyAlarm(this, "test", timeStr)
        
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content), 
            "Test alarm scheduled for $timeStr", 
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun startStepService() {
        android.util.Log.d("SettingsActivity", "Starting step service")
        
        // Check available sensors
        val sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)
        val stepDetectorSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR)
        val accelerometerSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        
        val sensorMessage = when {
            stepCounterSensor != null -> {
                android.util.Log.d("SettingsActivity", "Using hardware step counter sensor")
                "Step counting enabled (Hardware Step Counter)"
            }
            stepDetectorSensor != null -> {
                android.util.Log.d("SettingsActivity", "Using hardware step detector sensor")
                "Step counting enabled (Hardware Step Detector)"
            }
            accelerometerSensor != null -> {
                android.util.Log.d("SettingsActivity", "Using accelerometer for step detection")
                "Step counting enabled (Accelerometer Detection)"
            }
            else -> {
                android.util.Log.e("SettingsActivity", "No sensors available for step detection")
                switchStepSensor.isChecked = false
                repo.saveSettings(repo.getSettings().copy(stepSensorEnabled = false))
                "No step detection sensors available on this device"
            }
        }
        
        if (stepCounterSensor == null && stepDetectorSensor == null && accelerometerSensor == null) {
            com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content),
                sensorMessage,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
            return
        }
        
        // Check for ACTIVITY_RECOGNITION permission (Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestActivityRecognitionPermission()
                return
            }
        }
        
        val intent = android.content.Intent(this, com.example.zen.sensors.StepService::class.java)
        startService(intent)
        
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content),
            sensorMessage,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun stopStepService() {
        android.util.Log.d("SettingsActivity", "Stopping step service")
        
        val intent = android.content.Intent(this, com.example.zen.sensors.StepService::class.java)
        stopService(intent)
        
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content),
            "Step counting disabled",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun requestActivityRecognitionPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    startStepService()
                } else {
                    switchStepSensor.isChecked = false
                    repo.saveSettings(repo.getSettings().copy(stepSensorEnabled = false))
                    com.google.android.material.snackbar.Snackbar.make(
                        findViewById(android.R.id.content),
                        "Activity recognition permission required for step counting",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun autoStartStepServiceIfNeeded() {
        // Check if device has step sensor
        val sensorManager = getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val stepSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)
        
        if (stepSensor == null) {
            return // No step sensor available
        }
        
        // Check for ACTIVITY_RECOGNITION permission (Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return // Permission not granted
            }
        }
        
        // Start the service silently
        val intent = android.content.Intent(this, com.example.zen.sensors.StepService::class.java)
        startService(intent)
        
        android.util.Log.d("SettingsActivity", "Auto-started step service")
    }

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION = 1001
    }
}
