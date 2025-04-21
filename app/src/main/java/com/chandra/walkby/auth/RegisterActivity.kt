package com.chandra.walkby.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chandra.walkby.R
import com.chandra.walkby.chat.ChatListActivity
import com.chandra.walkby.databinding.ActivityRegisterBinding
import com.chandra.walkby.repository.FirebaseRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private val repository = FirebaseRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up UI interactions
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            
            if (validateInput(name, email, password, confirmPassword)) {
                signUp(name, email, password)
            }
        }
        
        binding.loginTextView.setOnClickListener {
            finish()
        }
    }
    
    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        } else {
            binding.confirmPasswordInputLayout.error = null
        }
        
        return isValid
    }
    
    private fun signUp(name: String, email: String, password: String) {
        binding.registerButton.isEnabled = false
        
        lifecycleScope.launch {
            repository.signUp(email, password, name)
                .onSuccess {
                    navigateToChatList()
                }
                .onFailure { exception ->
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.registerButton.isEnabled = true
                }
        }
    }
    
    private fun navigateToChatList() {
        val intent = Intent(this, ChatListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 