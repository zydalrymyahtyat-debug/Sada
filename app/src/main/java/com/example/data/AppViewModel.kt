package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel {
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user1",
            name = "مستخدم جديد",
            bio = "أكتب ما بداخلي، ليتردد صداه.",
            avatar = "https://i.pravatar.cc/150?u=user1"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(
        listOf(
            Post(
                id = "p1",
                authorName = "أحمد محمد",
                authorAvatar = "https://i.pravatar.cc/150?u=ahmed",
                text = "جمعة مباركة على الجميع 🇾🇪\nما أجمل الصباح في صنعاء القديمة.",
                timestamp = "منذ ساعتين",
                likesCount = 15,
                isLiked = true,
                commentsCount = 3
            ),
            Post(
                id = "p2",
                authorName = "سالم اليافعي",
                authorAvatar = "https://i.pravatar.cc/150?u=salem",
                text = "صورة التقطتها اليوم من شواطئ المكلا الخلابة.",
                image = "https://images.unsplash.com/photo-1549282361-9c3f46f481c8",
                timestamp = "منذ 4 ساعات",
                likesCount = 42,
                commentsCount = 8
            )
        )
    )
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(
        listOf(
            Story(
                id = "s1",
                authorName = "قصتي",
                authorAvatar = "https://i.pravatar.cc/150?u=user1",
                image = "" // Placeholder for user's own story addition
            ),
            Story(
                id = "s2",
                authorName = "أحمد",
                authorAvatar = "https://i.pravatar.cc/150?u=ahmed",
                image = "https://images.unsplash.com/photo-1555627192-3e3e07dbd07e"
            ),
            Story(
                id = "s3",
                authorName = "سالم",
                authorAvatar = "https://i.pravatar.cc/150?u=salem",
                image = "https://images.unsplash.com/photo-1549282361-9c3f46f481c8"
            ),
             Story(
                id = "s4",
                authorName = "مريم",
                authorAvatar = "https://i.pravatar.cc/150?u=maryam",
                image = "https://images.unsplash.com/photo-1511216335778-7cb8f49fa7a3"
            )
        )
    )
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    fun addPost(text: String) {
        val user = _currentUser.value
        val newPost = Post(
            id = "p${System.currentTimeMillis()}",
            authorName = user.name,
            authorAvatar = user.avatar,
            text = text,
            timestamp = "الآن",
            likesCount = 0,
            commentsCount = 0
        )
        _posts.update { listOf(newPost) + it }
    }

    fun toggleLike(postId: String) {
        _posts.update { currentPosts ->
            currentPosts.map { post ->
                if (post.id == postId) {
                    val countChange = if (post.isLiked) -1 else 1
                    post.copy(
                        isLiked = !post.isLiked,
                        likesCount = post.likesCount + countChange
                    )
                } else {
                    post
                }
            }
        }
    }
    
    fun updateProfile(name: String, bio: String) {
        _currentUser.update {
            it.copy(name = name, bio = bio)
        }
    }
}
