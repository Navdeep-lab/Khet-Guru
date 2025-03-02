package com.example.khetguru.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.khetguru.ui.theme.CropManagement
import com.google.firebase.database.core.Context

@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stylish App Name
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "\uD83D\uDE9CKhetGuru",
            fontSize = 45.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF388E3C), // Deep Green
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // First Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp,
                              ), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(100.dp)
                ,colors = CardDefaults.cardColors(Color(0xFFBBDEFB)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { navController.navigate("weather") }
            ) {
                Row(modifier = Modifier.padding(10.dp)){
                    Text(
                        "     Weather", style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
//                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Centers content horizontally
                    verticalArrangement = Arrangement.Center // Centers content vertically
                ){ Text(
                    "🌤️ \uD83C\uDF27\uFE0F", // Emojis centered below the text
                    fontSize = 40.sp // Increases the size of the emojis
                )}
            }
            Card( modifier = Modifier
                .width(150.dp)
                .height(100.dp)
                ,colors = CardDefaults.cardColors(Color(0xFFDECFCB)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { navController.navigate("soil_health") }
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "   Soil Health", style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
//                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Centers content horizontally
                    verticalArrangement = Arrangement.Center // Centers content vertically
                ){ Text(
                    "\uD83C\uDF31", // Emojis centered below the text
                    fontSize = 40.sp // Increases the size of the emojis
                )}
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(100.dp)
                ,colors = CardDefaults.cardColors(Color(0xFFC8E6C9)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { navController.navigate("crop_updates") }

            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "  Crop Updates", style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
//                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Centers content horizontally
                    verticalArrangement = Arrangement.Center // Centers content vertically
                ){ Text(
                    "\uD83E\uDDD1\u200D\uD83C\uDF3E", // Emojis centered below the text
                    fontSize = 40.sp // Increases the size of the emojis
                )}
            }
            Card( modifier = Modifier
                .width(150.dp)
                .height(100.dp)
                ,colors = CardDefaults.cardColors(Color(0xFFFDDEE1)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { navController.navigate("market_prices") }
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "  Market Price", style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
//                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Centers content horizontally
                    verticalArrangement = Arrangement.Center // Centers content vertically
                ){ Text(
                    "\uD83D\uDCB0", // Emojis centered below the text
                    fontSize = 40.sp // Increases the size of the emojis
                )}
            }

        }
        // Crop Management Section
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "\uD83C\uDF3FCrop Management",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C), // Green color
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CropManagementCard(navController)

        // Weather Forecast
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "🌦️ Weather Forecast",
            fontSize = 28.sp, // Slightly larger for emphasis
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF01579B), // Elegant dark blue color
            textAlign = TextAlign.Center,
           // Adds a subtle shadow effect
        )

        WeatherCard(navController)

        Spacer(modifier = Modifier.height(20.dp))
        // Soil & Pest Analysis
        Text(
            text = "🌱 Soil & Pest Analysis 🐞",
            fontSize = 24.sp, // Slightly larger for emphasis
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF6D4C41), // Elegant earthy brown color
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp, // Enhances readability
        )

        SoilAnalysisCard(navController)

        Spacer(modifier = Modifier.height(20.dp))
        // Market Prices
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF4CAF50),fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)) { // Green Money Emoji
                    append("💰 ")
                }
                withStyle(style = SpanStyle(color = Color(0xFFD32F2F), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)) { // Blue Main Text
                    append("Market Prices")
                }
                withStyle(style = SpanStyle(color = Color(0xFFFF9800), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)) { // Orange Card Emoji
                    append(" 💳")
                }
            },
            textAlign = TextAlign.Center,
        )
        MarketPriceCard(navController)
        Spacer(modifier = Modifier.height(20.dp))
        
    }
}


@Composable
fun DashboardCard(title: String, subtitle: String = "", onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CropManagementCard(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)) // Light green background
        ) {
            Column(
                modifier = Modifier.padding(13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "☘\uFE0F Your Crops",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Button(
                        onClick = { navController.navigate("crop_management") },
                        modifier = Modifier.size(50.dp), // Circular button
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp) // Ensures content stays centered
                    ) {
                        Text(
                            text = "➕",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterVertically) // Centers inside Button
                        )
                    }

                }

                //Spacer(modifier = Modifier.height(2.dp))

                // Crop List
                Column {
                    CropItem(icon = "🌾", name = "Wheat")
                    Spacer(modifier = Modifier.height(8.dp))
                    CropItem(icon = "🌽", name = "Corn")
                    Spacer(modifier = Modifier.height(8.dp))
                    CropItem(icon = "🍚", name = "Rice")

                }
            }
        }
    }
}

@Composable
fun CropItem(icon: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = icon,
            fontSize = 30.sp,
            // Increase the size of the emoji
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between emoji and text
        Text(
            text = name,
            fontSize = 18.sp, // Normal text size
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun WeatherCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC)), // Light blue background
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌦️Check Weather",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF01579B) // Dark Blue Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Partly Cloudy, 25°C",
                fontSize = 18.sp,
                color = Color(0xFF0277BD) // Slightly lighter blue text
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Button
            Button(
                onClick = { navController.navigate("weather_forecast") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(50)
            ) {
                Text("View Details", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun SoilAnalysisCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5D8D0)), // Light earthy tone
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "🌱 Soil Fertility",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6D4C41), // Elegant earthy brown color
                textAlign = TextAlign.Center
            )

            // Description
            Text(
                text = "Check your soil health and fertility level to optimize crop yield.",
                fontSize = 16.sp,
                color = Color(0xFF8C685D),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Navigation Button
            Button(
                onClick = { navController.navigate("soil_health") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58A45B)),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .height(45.dp)
                    .width(180.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(text = "Analyze Soil", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MarketPriceCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(10.dp)
            .clickable { navController.navigate("market_prices") }, // Navigation on click
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0E0)), // Light Pink Background
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Market Prices Title
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFD32F2F), fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append("💰 See Market")
                    }
                },
                textAlign = TextAlign.Center
            )

            // Market Trends Emoji
            Text(
                text = "📉📈📊",
                fontSize = 30.sp,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // View Prices Button
            Button(
                onClick = { navController.navigate("market_prices") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "View Prices", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun Navigation(viewModel: MarketPriceViewModel,weatherViewModel: WeatherViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("weather") { WeatherForecastScreen(weatherViewModel) }
        composable("soil_health") {
            val context = LocalContext.current
            SoilHealthScreen(context = context) }
        composable("crop_updates") { CropManagement(navController) }
        composable("market_prices") { MarketPriceScreen(viewModel, navController) }
        composable("crop_management") { CropManagement(navController) }
        composable("weather_forecast") { WeatherForecastScreen(weatherViewModel) }
        composable("saved_prices") { SavedPricesScreen(viewModel,navController) }
    }
}


