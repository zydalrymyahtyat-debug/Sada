package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import coil.compose.AsyncImage
import com.example.data.AppViewModel
import com.example.ui.theme.PrimaryBlue
import com.example.utils.ImageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.name) }
    var editBio by remember { mutableStateOf(user.bio) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            isUpdating = true
            scope.launch {
                val b64 = ImageUtils.uriToBase64(context, uri)
                if (b64 != null) {
                    val finalString = "data:image/jpeg;base64,$b64"
                    viewModel.updateAvatar(finalString)
                }
                isUpdating = false
            }
        }
    }

    LaunchedEffect(user) {
        if (!isEditing) {
            editName = user.name
            editBio = user.bio
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(bottom = 16.dp)) {
                    val avatarUrl = user.avatar
                    if (avatarUrl.startsWith("data:image")) {
                        val base64Str = avatarUrl.substringAfter(",")
                        val decodedBytes = remember(base64Str) { try { Base64.decode(base64Str, Base64.DEFAULT) } catch(e: Exception) { null } }
                        val bitmap = remember(decodedBytes) { decodedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, PrimaryBlue, CircleShape)
                            )
                        } else {
                             Box(modifier = Modifier.size(100.dp).background(Color.LightGray, CircleShape).border(3.dp, PrimaryBlue, CircleShape))
                        }
                    } else {
                        AsyncImage(
                            model = if (avatarUrl.isNotBlank()) avatarUrl else "https://via.placeholder.com/150",
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, PrimaryBlue, CircleShape)
                        )
                    }
                    
                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Edit Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                    
                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, editBio)
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("حفظ التغييرات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Text(text = user.name.ifEmpty { "مستخدم جديد" }, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = user.bio, 
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                        border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("تعديل الملف", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
