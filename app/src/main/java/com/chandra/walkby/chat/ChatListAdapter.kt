package com.chandra.walkby.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chandra.walkby.databinding.ItemChatBinding
import com.chandra.walkby.model.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val currentUserId: String,
    private val onChatClicked: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {
    
    private val chats = mutableListOf<Chat>()
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }
    
    override fun getItemCount() = chats.size
    
    fun addChat(chat: Chat) {
        if (!chats.contains(chat)) {
            chats.add(chat)
            // Sort by last message time, newest first
            chats.sortByDescending { it.lastMessageTime }
            notifyDataSetChanged()
        }
    }
    
    fun updateChat(chat: Chat) {
        val index = chats.indexOfFirst { it.id == chat.id }
        if (index != -1) {
            chats[index] = chat
            // Sort by last message time, newest first
            chats.sortByDescending { it.lastMessageTime }
            notifyDataSetChanged()
        } else {
            addChat(chat)
        }
    }
    
    fun removeChat(chat: Chat) {
        val index = chats.indexOfFirst { it.id == chat.id }
        if (index != -1) {
            chats.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChatClicked(chats[position])
                }
            }
        }
        
        fun bind(chat: Chat) {
            // Get the other user's email
            val otherUserEmail = chat.participantEmails.entries
                .find { it.key != currentUserId }
                ?.value ?: "Unknown"
            
            binding.userEmailTextView.text = otherUserEmail
            
            // Show last message if available
            binding.lastMessageTextView.text = if (chat.lastMessage.isNotEmpty()) {
                // Show "You: " prefix if current user sent the last message
                if (chat.lastMessageSender == currentUserId) {
                    "You: ${chat.lastMessage}"
                } else {
                    chat.lastMessage
                }
            } else {
                "No messages yet"
            }
            
            // Format and show timestamp
            binding.timestampTextView.text = if (chat.lastMessageTime > 0) {
                dateFormat.format(Date(chat.lastMessageTime))
            } else {
                ""
            }
        }
    }
} 