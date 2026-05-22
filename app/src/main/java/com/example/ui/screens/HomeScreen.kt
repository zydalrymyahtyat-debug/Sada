package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.AppViewModel
import com.example.data.Post
import com.example.data.Story
import com.example.ui.theme.PrimaryBlue
import com.example.utils.ImageUtils
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Stories Section
        item {
            StoriesRow(stories = stories)
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        // Post Creation Section
        item {
            CreatePostCard(
                userAvatar = user.avatar,
                onPostSubmit = { text, imageBase64 -> viewModel.addPost(text, imageBase64) }
            )
        }

        // Feed
        items(posts, key = { it.id }) { post ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 200 }),
                exit = fadeOut()
            ) {
                PostCard(
                    post = post,
                    onLikeClick = { viewModel.toggleLike(post.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun StoriesRow(stories: List<Story>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(65.dp)
            ) {
                if (story.id == "s1") {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(2.dp, PrimaryBlue, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Story", tint = PrimaryBlue, modifier = Modifier.size(30.dp))
                    }
                } else {
                    AsyncImage(
                        model = story.image.ifEmpty { story.authorAvatar },
                        contentDescription = "Story",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .border(3.dp, PrimaryBlue, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.authorName.split(" ").first(),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostCard(userAvatar: String, onPostSubmit: (String, String?) -> Unit) {
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var gettingImage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = userAvatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("إيش في خاطرك اليوم؟") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            AnimatedVisibility(visible = selectedImageUri != null) {
                Box(modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text("X", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${postText.length}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { galleryLauncher.launch("image/*") }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "الصورة", tint = Color(0xFF2ECC71), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صورة", color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if(postText.isNotBlank() || selectedImageUri != null) {
                                gettingImage = true
                                scope.launch {
                                    val b64 = selectedImageUri?.let { ImageUtils.uriToBase64(context, it) }
                                    onPostSubmit(postText, b64)
                                    postText = ""
                                    selectedImageUri = null
                                    gettingImage = false
                                }
                            }
                        },
                        enabled = (postText.isNotBlank() || selectedImageUri != null) && !gettingImage,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        if (gettingImage) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("أرسل صداك", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, onLikeClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = post.authorAvatar,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = post.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        val timestampString = remember(post.timestamp) {
                            val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(post.timestamp))
                        }
                        Text(text = timestampString, color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = MaterialTheme.colorScheme.tertiary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Text(
                text = post.text,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            
            val imageData = post.image
            if (imageData != null && imageData.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                if (imageData.startsWith("http")) {
                    AsyncImage(
                        model = imageData,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                    )
                } else {
                    val decodedBytes = remember(imageData) { 
                        try {
                            Base64.decode(imageData, Base64.DEFAULT) 
                        } catch(e: Exception) { 
                            null 
                        }
                    }
                    val bitmap = remember(decodedBytes) {
                        if (decodedBytes != null) {
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        } else null
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${post.likesCount} وصلهم الصدى", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                Text(text = "${post.commentsCount} أصداء", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }.padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "تفاعل", 
                        color = if (post.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Outlined.Comment, contentDescription = "Comment", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تعليق", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}
