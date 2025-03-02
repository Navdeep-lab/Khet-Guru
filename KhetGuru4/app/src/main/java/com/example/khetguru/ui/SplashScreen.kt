package com.example.khetguru.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.khetguru.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController) {
    // Delayed Navigation
    LaunchedEffect(key1 = true) {
        delay(2000) // 3 seconds delay
        navController.navigate("dashboard") {
            popUpTo("splash") { inclusive = true }
        }
    }

    // UI for Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Set background to white
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.farm1), // Ensure the image is in res/drawable
                contentDescription = "Khet Guru Logo",
                modifier = Modifier
                    .size(180.dp) // Increased size for better visibility
                    .clip(CircleShape) // Keeps the image circular
                    .border(3.dp, Color(0xFF388E3C), CircleShape) // Thicker green border for better aesthetics
                    .shadow(8.dp, CircleShape) // Adds a soft shadow for a premium look
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space between image and text
            Text(
                text = "Khet Guru",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF388E3C), // Deep Green
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

