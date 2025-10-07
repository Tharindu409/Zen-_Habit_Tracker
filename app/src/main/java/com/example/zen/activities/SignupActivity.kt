package com.example.zen.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextWatcher
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zen.R
import com.example.zen.models.UserProfile
import com.example.zen.repo.ZenRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var repository: ZenRepository
    private lateinit var tilEmoji: TextInputLayout
    private lateinit var etEmoji: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var tilAge: TextInputLayout
    private lateinit var etAge: TextInputEditText
    private lateinit var tilGender: TextInputLayout
    private lateinit var spGender: MaterialAutoCompleteTextView
    private lateinit var btnSignup: MaterialButton
    
    private var selectedAvatarEmoji = "🫣" // Default emoji

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        
        repository = ZenRepository.getInstance(this)
        
        initViews()
        setupInputValidation()
        setupGenderSpinner()
        setupClickListeners()
    }

    private fun initViews() {
        tilEmoji = findViewById(R.id.tilEmoji)
        etEmoji = findViewById(R.id.etEmoji)
        tilName = findViewById(R.id.tilName)
        etName = findViewById(R.id.etName)
        tilAge = findViewById(R.id.tilAge)
        etAge = findViewById(R.id.etAge)
        tilGender = findViewById(R.id.tilGender)
        spGender = findViewById(R.id.spGender)
        btnSignup = findViewById(R.id.btnSignup)
        
        etEmoji.setText(selectedAvatarEmoji)
    }

    private fun setupInputValidation() {
        // Emoji validation
        etEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmoji()
            }
        })
        
        // Name: Only letters and spaces
        val nameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val filtered = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                if (char.isLetter() || char == ' ') {
                    filtered.append(char)
                }
            }
            if (filtered.length == end - start) {
                null // Accept all characters
            } else {
                filtered.toString() // Return filtered string
            }
        }
        etName.filters = arrayOf(nameFilter, InputFilter.LengthFilter(50))
        
        // Age: Only positive numbers, max 3 digits
        val ageFilter = InputFilter { source, start, end, dest, dstart, dend ->
            try {
                val input = dest.toString().substring(0, dstart) + 
                           source.subSequence(start, end) + 
                           dest.toString().substring(dend)
                
                if (input.isEmpty()) return@InputFilter null
                
                val age = input.toInt()
                if (age > 0 && age <= 150 && input.length <= 3) {
                    null // Accept
                } else {
                    "" // Reject
                }
            } catch (e: NumberFormatException) {
                "" // Reject invalid numbers
            }
        }
        etAge.filters = arrayOf(ageFilter)
        
        // Add real-time validation
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateName()
            }
        })
        
        etAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAge()
            }
        })
    }

    private fun setupGenderSpinner() {
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        spGender.setAdapter(adapter)
        
        spGender.setOnItemClickListener { _, _, _, _ ->
            validateGender()
        }
    }

    private fun setupClickListeners() {
        btnSignup.setOnClickListener {
            if (validateAllInputs()) {
                saveProfile()
            }
        }
    }
    
    private fun validateEmoji(): Boolean {
        val emoji = etEmoji.text?.toString()?.trim()
        return if (emoji.isNullOrEmpty()) {
            tilEmoji.error = "Please enter an emoji"
            false
        } else if (emoji.length > 2) {
            tilEmoji.error = "Please enter only one emoji"
            false
        } else {
            tilEmoji.error = null
            selectedAvatarEmoji = emoji
            true
        }
    }

    private fun validateName(): Boolean {
        val name = etName.text?.toString()?.trim()
        return if (name.isNullOrEmpty()) {
            tilName.error = "Name is required"
            false
        } else if (name.length < 2) {
            tilName.error = "Name must be at least 2 characters"
            false
        } else {
            tilName.error = null
            true
        }
    }

    private fun validateAge(): Boolean {
        val ageText = etAge.text?.toString()?.trim()
        return if (ageText.isNullOrEmpty()) {
            tilAge.error = "Age is required"
            false
        } else {
            val age = ageText.toIntOrNull()
            if (age == null || age < 1 || age > 150) {
                tilAge.error = "Age must be between 1 and 150"
                false
            } else {
                tilAge.error = null
                true
            }
        }
    }

    private fun validateGender(): Boolean {
        val gender = spGender.text?.toString()?.trim()
        return if (gender.isNullOrEmpty()) {
            tilGender.error = "Gender is required"
            false
        } else {
            tilGender.error = null
            true
        }
    }

    private fun validateAllInputs(): Boolean {
        val isEmojiValid = validateEmoji()
        val isNameValid = validateName()
        val isAgeValid = validateAge()
        val isGenderValid = validateGender()
        
        return isEmojiValid && isNameValid && isAgeValid && isGenderValid
    }

    private fun saveProfile() {
        val name = etName.text?.toString()?.trim() ?: ""
        val age = etAge.text?.toString()?.trim()?.toIntOrNull() ?: 0
        val gender = spGender.text?.toString()?.trim() ?: ""
        
        val profile = UserProfile(
            name = name,
            age = age,
            gender = gender,
            timezoneId = TimeZone.getDefault().id, // Use system default
            avatarEmoji = selectedAvatarEmoji
        )
        
        repository.saveUserProfile(profile)
        
        // Navigate to home
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
