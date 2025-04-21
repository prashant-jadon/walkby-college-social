package com.chandra.walkby.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chandra.walkby.R
import com.chandra.walkby.databinding.ItemMessageBinding
import com.chandra.walkby.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    
    private val messages = mutableListOf<Message>()
    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount() = messages.size
    
    fun addMessage(message: Message) {
        if (!messages.contains(message)) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
        }
    }
    
    fun updateMessage(message: Message) {
        val index = messages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            messages[index] = message
            notifyItemChanged(index)
        } else {
            addMessage(message)
        }
    }
    
    fun removeMessage(message: Message) {
        val index = messages.indexOfFirst { it.id == message.id }
        if (index != -1) {
            messages.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    
    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            // Set the message text
            binding.messageTextView.text = message.text
            
            // Format and show timestamp
            binding.timestampTextView.text = dateFormat.format(Date(message.timestamp))
            
            // Style message based on sender
            val isCurrentUserMessage = message.senderId == currentUserId
            
            // Apply different styling for sent vs received messages
            if (isCurrentUserMessage) {
                // Align to the right for current user messages
                binding.messageCardView.apply {
                    val params = binding.messageCardView.layoutParams as ViewGroup.MarginLayoutParams
                    
                    // Reset left margin and set right margin to 8dp
                    params.marginStart = 60 * context.resources.displayMetrics.density.toInt()
                    params.marginEnd = 8 * context.resources.displayMetrics.density.toInt()
                    
                    layoutParams = params
                    
                    // Set the card background color to a primary color
                    setCardBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_blue_light)
                    )
                }
                
                // Set text color to white for better contrast
                binding.messageTextView.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.timestampTextView.setTextColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
            } else {
                // Default styling is already set in the layout for received messages
            }
        }
    }
} 