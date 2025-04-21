package com.chandra.walkby.chat

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.walkby.databinding.ActivityChatBinding
import com.chandra.walkby.model.Message
import com.chandra.walkby.repository.FirebaseRepository
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_CHAT_ID = "chat_id"
        const val EXTRA_OTHER_USER_EMAIL = "other_user_email"
    }
    
    private lateinit var binding: ActivityChatBinding
    private val repository = FirebaseRepository()
    private lateinit var adapter: MessageAdapter
    
    private var chatId: String = ""
    private var otherUserEmail: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: ""
        otherUserEmail = intent.getStringExtra(EXTRA_OTHER_USER_EMAIL) ?: ""
        
        if (chatId.isEmpty()) {
            finish()
            return
        }
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = otherUserEmail
            setDisplayHomeAsUpEnabled(true)
        }
        
        // Set up recycler view
        adapter = MessageAdapter(repository.currentUser?.uid ?: "")
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        
        // Set up send button
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }
        
        // Listen for messages
        listenForMessages()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun sendMessage(text: String) {
        binding.messageEditText.text?.clear()
        
        lifecycleScope.launch {
            repository.sendMessage(chatId, text)
                .onFailure { exception ->
                    Toast.makeText(
                        this@ChatActivity,
                        "Error sending message: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
    
    private fun listenForMessages() {
        repository.getChatMessages(chatId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            
            snapshot?.documentChanges?.forEach { docChange ->
                val message = docChange.document.toObject(Message::class.java)
                
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        adapter.addMessage(message)
                        binding.messagesRecyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                    DocumentChange.Type.MODIFIED -> adapter.updateMessage(message)
                    DocumentChange.Type.REMOVED -> adapter.removeMessage(message)
                }
            }
        }
    }
} 