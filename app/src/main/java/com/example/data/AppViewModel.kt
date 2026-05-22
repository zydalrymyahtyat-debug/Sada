package com.example.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(
        listOf(
            Story(
                id = "s1",
                authorName = "قصتي",
                image = "" 
            )
        )
    )
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Fetch profile
                db.collection("users").document(user.uid).addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        try {
                            val profile = snapshot.toObject(UserProfile::class.java)
                            if (profile != null) {
                                profile.id = snapshot.id
                                _currentUser.value = profile
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                fetchPosts()
                fetchStories()
            }
        }
    }

    private fun fetchPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    try {
                        val postsList = snapshot.documents.mapNotNull { doc ->
                            val post = doc.toObject(Post::class.java)
                            post?.apply {
                                id = doc.id
                                isLiked = likes.contains(uid)
                                likesCount = likes.size
                            }
                        }
                        _posts.value = postsList
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    private fun fetchStories() {
        val yesterdayDate = java.util.Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        db.collection("stories")
            .whereGreaterThanOrEqualTo("timestamp", yesterdayDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    try {
                        val defaultStory = Story(id = "s1", authorName = "قصتي", image = "")
                        val storiesList = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Story::class.java)?.apply { id = doc.id } 
                        }
                        
                        val distinctStories = storiesList.distinctBy { it.uid }
                        _stories.value = listOf(defaultStory) + distinctStories
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    fun addPost(text: String, imageBase64: String? = null) {
        val user = auth.currentUser ?: return
        val profile = _currentUser.value
        
        val newPost = Post(
            authorName = profile.name.ifEmpty { "مستخدم" },
            authorAvatar = profile.avatar,
            text = text,
            image = imageBase64,
            uid = user.uid,
            timestamp = null
        )
        
        db.collection("posts").add(newPost)
    }

    fun addStory(imageBase64: String) {
        val user = auth.currentUser ?: return
        val profile = _currentUser.value
        val story = Story(
            authorName = profile.name.ifEmpty { "مستخدم" },
            authorAvatar = profile.avatar,
            image = imageBase64,
            uid = user.uid,
            timestamp = null
        )
        db.collection("stories").add(story)
    }

    fun updateAvatar(avatarBase64: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("avatar", avatarBase64)
    }

    fun toggleLike(postId: String) {
        val uid = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(postId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likes = snapshot.get("likes") as? List<String> ?: emptyList()
            if (likes.contains(uid)) {
                transaction.update(postRef, "likes", likes - uid)
            } else {
                transaction.update(postRef, "likes", likes + uid)
            }
        }
    }

    fun updateProfile(name: String, bio: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(
            mapOf("name" to name, "bio" to bio)
        )
    }
}
