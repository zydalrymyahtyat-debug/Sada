package com.example.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
                        val profile = snapshot.toObject(UserProfile::class.java)
                        if (profile != null) {
                            _currentUser.value = profile.copy(id = snapshot.id)
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
                    val postsList = snapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)
                        post?.let {
                            it.copy(
                                id = doc.id,
                                isLiked = it.likes.contains(uid),
                                likesCount = it.likes.size
                            )
                        }
                    }
                    _posts.value = postsList
                }
            }
    }

    private fun fetchStories() {
        val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        db.collection("stories")
            .whereGreaterThan("timestamp", yesterday)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val defaultStory = Story(id = "s1", authorName = "قصتي", image = "")
                    val storiesList = snapshot.documents.mapNotNull { it.toObject(Story::class.java)?.copy(id = it.id) }
                    
                    val distinctStories = storiesList.distinctBy { it.uid }
                    _stories.value = listOf(defaultStory) + distinctStories
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
            timestamp = System.currentTimeMillis()
        )
        
        db.collection("posts").add(newPost)
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
