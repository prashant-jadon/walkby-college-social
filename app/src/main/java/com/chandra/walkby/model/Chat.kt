package com.chandra.walkby.model

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantEmails: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val lastMessageSender: String = ""
) 