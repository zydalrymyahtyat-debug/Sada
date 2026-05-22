package com.example.data

data class Post(
    var id: String = "",
    var authorName: String = "",
    var authorAvatar: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var likesCount: Int = 0,
    var isLiked: Boolean = false,
    var commentsCount: Int = 0,
    var image: String? = null,
    var uid: String = "",
    var likes: List<String> = emptyList()
)

data class UserProfile(
    var id: String = "",
    var name: String = "",
    var bio: String = "أكتب ما بداخلي، ليتردد صداه.",
    var avatar: String = "",
    var savedPosts: List<String> = emptyList()
)

data class Story(
    var id: String = "",
    var authorName: String = "",
    var authorAvatar: String = "",
    var image: String = "",
    var uid: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    var id: String = "",
    var senderId: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var senderName: String = "",
    var senderAvatar: String = ""
)
