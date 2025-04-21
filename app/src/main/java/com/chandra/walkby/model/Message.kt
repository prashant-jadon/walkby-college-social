package com.chandra.walkby.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
) 