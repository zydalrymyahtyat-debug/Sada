package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.data.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup Firebase Programmatically using the user's config
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBSATw2czEW8Mo0SXTJeBrBUjOe5J9FLyk")
                .setApplicationId("1:292791351147:web:4c3c168eefbb41fd0dbe2e")
                .setProjectId("al-muna-9872e")
                .setDatabaseUrl("https://al-muna-9872e.firebaseio.com")
                .setStorageBucket("al-muna-9872e.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val auth = FirebaseAuth.getInstance()
                    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
                    val viewModel: AppViewModel = viewModel()

                    if (isLoggedIn) {
                        MainAppScreen(viewModel = viewModel, onLogout = { 
                            auth.signOut()
                            isLoggedIn = false 
                        })
                    } else {
                        AuthScreen(
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
}
