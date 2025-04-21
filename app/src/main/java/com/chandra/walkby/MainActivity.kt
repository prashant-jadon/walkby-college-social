package com.chandra.walkby

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chandra.walkby.auth.LoginActivity
import com.chandra.walkby.repository.FirebaseRepository

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect to the appropriate screen
        val repository = FirebaseRepository()
        
        if (repository.currentUser != null) {
            // User is already logged in, go to chat list
            startActivity(Intent(this, com.chandra.walkby.chat.ChatListActivity::class.java))
        } else {
            // User is not logged in, go to login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        finish()
    }
}