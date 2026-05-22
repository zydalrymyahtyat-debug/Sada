package com.example.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    @get:Exclude @set:Exclude var id: String = "",
    var authorName: String = "",
    var authorAvatar: String = "",
    var text: String = "",
    @ServerTimestamp var timestamp: Date? = null,
    @get:Exclude @set:Exclude var likesCount: Int = 0,
    @get:Exclude @set:Exclude var isLiked: Boolean = false,
    var commentsCount: Int = 0,
    var image: String? = null,
    var uid: String = "",
    var likes: List<String> = emptyList()
)

data class UserProfile(
    @get:Exclude @set:Exclude var id: String = "",
    var name: String = "",
    var bio: String = "أكتب ما بداخلي، ليتردد صداه.",
    var avatar: String = "",
    var savedPosts: List<String> = emptyList()
)

data class Story(
    @get:Exclude @set:Exclude var id: String = "",
    var authorName: String = "",
    var authorAvatar: String = "",
    var image: String = "",
    var uid: String = "",
    @ServerTimestamp var timestamp: Date? = null
)

data class ChatMessage(
    @get:Exclude @set:Exclude var id: String = "",
    var senderId: String = "",
    var text: String = "",
    @ServerTimestamp var timestamp: Date? = null,
    var senderName: String = "",
    var senderAvatar: String = ""
)
