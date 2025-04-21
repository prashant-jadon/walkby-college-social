package com.chandra.walkby.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chandra.walkby.R
import com.chandra.walkby.auth.LoginActivity
import com.chandra.walkby.databinding.ActivityChatListBinding
import com.chandra.walkby.databinding.DialogNewChatBinding
import com.chandra.walkby.model.Chat
import com.chandra.walkby.repository.FirebaseRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChatListBinding
    private val repository = FirebaseRepository()
    private lateinit var adapter: ChatListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        
        // Check if user is logged in
        if (repository.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Set up recycler view
        adapter = ChatListAdapter(repository.currentUser?.uid ?: "") { chat ->
            openChat(chat)
        }
        
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Set up FAB
        binding.newChatFab.setOnClickListener {
            showNewChatDialog()
        }
        
        // Listen for chats
        listenForChats()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_list, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                repository.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun listenForChats() {
        repository.getUserChats().addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            
            if (snapshot != null) {
                processChatsSnapshot(snapshot)
            }
        }
    }
    
    private fun processChatsSnapshot(snapshot: QuerySnapshot) {
        for (docChange in snapshot.documentChanges) {
            val chat = docChange.document.toObject(Chat::class.java)
            
            when (docChange.type) {
                DocumentChange.Type.ADDED -> adapter.addChat(chat)
                DocumentChange.Type.MODIFIED -> adapter.updateChat(chat)
                DocumentChange.Type.REMOVED -> adapter.removeChat(chat)
            }
        }
        
        updateEmptyView()
    }
    
    private fun updateEmptyView() {
        if (adapter.itemCount == 0) {
            binding.emptyTextView.visibility = View.VISIBLE
            binding.chatRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTextView.visibility = View.GONE
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showNewChatDialog() {
        val dialogBinding = DialogNewChatBinding.inflate(layoutInflater)
        
        AlertDialog.Builder(this)
            .setTitle("Start New Chat")
            .setView(dialogBinding.root)
            .setPositiveButton("Start Chat") { _, _ ->
                val email = dialogBinding.emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    startNewChat(email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startNewChat(email: String) {
        // Don't allow starting chat with yourself
        if (email == repository.currentUser?.email) {
            return
        }
        
        lifecycleScope.launch {
            repository.createChat(email)
                .onSuccess { chat ->
                    openChat(chat)
                }
                .onFailure { exception ->
                    showError("Could not create chat: ${exception.message}")
                }
        }
    }
    
    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CHAT_ID, chat.id)
            
            // Find the other user's email
            val currentUserId = repository.currentUser?.uid ?: return
            val otherUserEmail = chat.participantEmails.entries
                .find { it.key != currentUserId }
                ?.value ?: ""
            
            putExtra(ChatActivity.EXTRA_OTHER_USER_EMAIL, otherUserEmail)
        }
        startActivity(intent)
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
} 