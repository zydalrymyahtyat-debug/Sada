package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppViewModel

@Composable
fun MessagesScreen() {
    // Dummy messages list
    val users = listOf(
        Pair("مريم", "https://i.pravatar.cc/150?u=maryam"),
        Pair("سالم اليافعي", "https://i.pravatar.cc/150?u=salem")
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users.size) { index ->
            val user = users[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Navigate to chat */ }
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = user.second,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user.first, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "اضغط لبدء المراسلة", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                }
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
