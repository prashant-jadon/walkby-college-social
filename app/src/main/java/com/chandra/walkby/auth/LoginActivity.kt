package com.chandra.walkby.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chandra.walkby.R
import com.chandra.walkby.chat.ChatListActivity
import com.chandra.walkby.databinding.ActivityLoginBinding
import com.chandra.walkby.repository.FirebaseRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val repository = FirebaseRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if user is already logged in
        if (repository.currentUser != null) {
            navigateToChatList()
            return
        }
        
        // Set up UI interactions
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            
            if (validateInput(email, password)) {
                signIn(email, password)
            }
        }
        
        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else {
            binding.passwordInputLayout.error = null
        }
        
        return isValid
    }
    
    private fun signIn(email: String, password: String) {
        binding.loginButton.isEnabled = false
        
        lifecycleScope.launch {
            repository.signIn(email, password)
                .onSuccess {
                    navigateToChatList()
                }
                .onFailure { exception ->
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.loginButton.isEnabled = true
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