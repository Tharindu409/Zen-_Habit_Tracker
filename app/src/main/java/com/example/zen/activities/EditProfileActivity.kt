package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.zen.R
import com.example.zen.models.UserProfile
import com.example.zen.repo.ZenRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditProfileActivity : AppCompatActivity() {

    private lateinit var repo: ZenRepository

    private lateinit var tilEmoji: TextInputLayout
    private lateinit var etEmoji: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilAge: TextInputLayout
    private lateinit var etAge: TextInputEditText
    private lateinit var tilGender: TextInputLayout
    private lateinit var spGender: MaterialAutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private var bottomNav: BottomNavigationView? = null

    private var avatarEmoji: String = "🫣"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        repo = ZenRepository.getInstance(this)
        bindViews()
        setupGenderSpinner()
        populateFromProfile()
        bindActions()
        setupToolbar()
        setupBottomNav()
    }

    private fun bindViews() {
        tilEmoji = findViewById(R.id.tilEmoji)
        etEmoji = findViewById(R.id.etEmoji)
        tilName = findViewById(R.id.tilName)
        etName = findViewById(R.id.etName)
        tilAge = findViewById(R.id.tilAge)
        etAge = findViewById(R.id.etAge)
        tilGender = findViewById(R.id.tilGender)
        spGender = findViewById(R.id.spGender)
        btnSave = findViewById(R.id.btnSave)
        bottomNav = findViewById(R.id.bottomNav)
    }

    private fun setupGenderSpinner() {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        spGender.setAdapter(adapter)
    }

    private fun populateFromProfile() {
        val p: UserProfile? = repo.getUserProfile()
        if (p != null) {
            avatarEmoji = p.avatarEmoji
            etEmoji.setText(p.avatarEmoji)
            etName.setText(p.name)
            etAge.setText(p.age.toString())
            spGender.setText(p.gender, false)
        } else {
            etEmoji.setText(avatarEmoji)
        }
    }

    private fun bindActions() {
        btnSave.setOnClickListener {
            if (validate()) {
                avatarEmoji = etEmoji.text?.toString()?.trim() ?: "🫣"
                val updated = UserProfile(
                    name = etName.text?.toString()?.trim() ?: "",
                    age = etAge.text?.toString()?.toIntOrNull() ?: 0,
                    gender = spGender.text?.toString()?.trim() ?: "",
                    timezoneId = java.util.TimeZone.getDefault().id, // Use system default
                    avatarEmoji = avatarEmoji
                )
                repo.saveUserProfile(updated)
                finish()
            }
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
                        startActivity(Intent(this@EditProfileActivity, HomeActivity::class.java))
                        finish(); true
                    }
                    R.id.menu_habits -> {
                        startActivity(Intent(this@EditProfileActivity, HabitsActivity::class.java))
                        finish(); true
                    }
                    R.id.menu_mood -> {
                        startActivity(Intent(this@EditProfileActivity, MoodActivity::class.java))
                        finish(); true
                    }
                    R.id.menu_profile -> {
                        startActivity(Intent(this@EditProfileActivity, ProfileActivity::class.java))
                        finish(); true
                    }
                    else -> false
                }
            }
        }
    }

    private fun validate(): Boolean {
        var ok = true
        
        val emoji = etEmoji.text?.toString()?.trim()
        if (emoji.isNullOrEmpty()) { 
            tilEmoji.error = "Emoji is required"
            ok = false 
        } else if (emoji.length > 2) {
            tilEmoji.error = "Please enter only one emoji"
            ok = false
        } else {
            tilEmoji.error = null
        }
        
        if (etName.text.isNullOrBlank()) { 
            tilName.error = getString(R.string.error_required_field)
            ok = false 
        } else {
            tilName.error = null
        }
        
        val age = etAge.text?.toString()?.toIntOrNull()
        if (age == null || age < 1 || age > 150) { 
            tilAge.error = getString(R.string.error_invalid_age)
            ok = false 
        } else {
            tilAge.error = null
        }
        
        if (spGender.text.isNullOrBlank()) { 
            tilGender.error = getString(R.string.error_required_field)
            ok = false 
        } else {
            tilGender.error = null
        }
        
        return ok
    }
}
