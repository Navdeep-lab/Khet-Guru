package com.example.khetguru

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.khetguru.ui.MarketPriceViewModel
import com.example.khetguru.ui.Navigation
import com.example.khetguru.ui.WeatherViewModel
import com.example.khetguru.ui.WeatherViewModelFactory
import com.example.khetguru.ui.requestLocation
import com.example.khetguru.ui.theme.KhetGuruTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val marketPriceViewModel: MarketPriceViewModel by viewModels()
    private lateinit var weatherViewModel: WeatherViewModel
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        requestLocation(this)
        weatherViewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(applicationContext)
        )[WeatherViewModel::class.java]

        setContent {
            KhetGuruTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Navigation(marketPriceViewModel,weatherViewModel) // ✅ Use Navigation() directly, no need to pass it to DashboardScreen
                }
            }
        }
    }
}
