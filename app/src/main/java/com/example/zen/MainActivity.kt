package com.example.zen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.zen.activities.HomeActivity
import com.example.zen.activities.SignupActivity
import com.example.zen.repo.ZenRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user has completed setup
        val repository = ZenRepository.getInstance(this)
        val userProfile = repository.getUserProfile()
        
        val targetActivity = if (userProfile != null) {
            HomeActivity::class.java
        } else {
            SignupActivity::class.java
        }
        
        val intent = Intent(this, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}