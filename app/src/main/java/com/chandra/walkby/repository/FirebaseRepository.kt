package com.chandra.walkby.repository

import com.chandra.walkby.model.Chat
import com.chandra.walkby.model.Message
import com.chandra.walkby.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    // Collections
    private val usersCollection = db.collection("users")
    private val chatsCollection = db.collection("chats")
    private val messagesCollection = db.collection("messages")
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    // Authentication methods
    suspend fun signUp(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            
            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            result.user?.updateProfile(profileUpdates)?.await()
            
            // Save user to Firestore
            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    createdAt = Date().time
                )
                usersCollection.document(firebaseUser.uid).set(user).await()
            }
            
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    // User methods
    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = usersCollection.whereEqualTo("email", email).get().await()
        return if (querySnapshot.documents.isNotEmpty()) {
            querySnapshot.documents[0].toObject<User>()
        } else {
            null
        }
    }
    
    // Chat methods
    suspend fun createChat(otherUserEmail: String): Result<Chat> {
        return try {
            val currentUserId = currentUser?.uid ?: throw IllegalStateException("User not logged in")
            
            // Find the other user by email
            val otherUser = getUserByEmail(otherUserEmail) ?: throw IllegalStateException("User not found")
            
            // Check if chat already exists between these users
            val existingChat = chatsCollection
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<Chat>() }
                .find { it.participants.contains(otherUser.uid) }
            
            if (existingChat != null) {
                return Result.success(existingChat)
            }
            
            // Create new chat
            val chatId = chatsCollection.document().id
            val participantsList = listOf(currentUserId, otherUser.uid)
            val participantEmails = mapOf(
                currentUserId to currentUser!!.email!!,
                otherUser.uid to otherUser.email
            )
            
            val chat = Chat(
                id = chatId,
                participants = participantsList,
                participantEmails = participantEmails,
                lastMessageTime = Date().time
            )
            
            chatsCollection.document(chatId).set(chat).await()
            Result.success(chat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getUserChats() = chatsCollection
        .whereArrayContains("participants", currentUser?.uid ?: "")
        .orderBy("lastMessageTime", Query.Direction.DESCENDING)
    
    // Message methods
    suspend fun sendMessage(chatId: String, text: String): Result<Message> {
        return try {
            val currentUserId = currentUser?.uid ?: throw IllegalStateException("User not logged in")
            
            val messageId = messagesCollection.document().id
            val now = Date().time
            
            val message = Message(
                id = messageId,
                chatId = chatId,
                senderId = currentUserId,
                text = text,
                timestamp = now
            )
            
            // Add message to database
            messagesCollection.document(messageId).set(message).await()
            
            // Update last message in chat
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to now,
                    "lastMessageSender" to currentUserId
                )
            ).await()
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getChatMessages(chatId: String) = messagesCollection
        .whereEqualTo("chatId", chatId)
        .orderBy("timestamp", Query.Direction.ASCENDING)
} 