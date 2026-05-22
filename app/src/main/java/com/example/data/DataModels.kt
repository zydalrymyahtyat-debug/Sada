package com.example.data

data class Post(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val image: String? = null
)

data class Story(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val image: String
)

data class UserProfile(
    val id: String,
    val name: String,
    val bio: String,
    val avatar: String,
    val savedPosts: List<String> = emptyList()
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isMe: Boolean
)
