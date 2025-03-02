@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.khetguru.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import com.example.khetguru.R
import com.example.khetguru.ui.RetrofitInstance.weatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Sealed class to handle network responses
sealed class NetworkResponse<out T> {
    data class Success<out T>(val data: T) : NetworkResponse<T>()
    data class Error(val message: String) : NetworkResponse<Nothing>()
    object Loading : NetworkResponse<Nothing>()
}

// Data models for the weather API response
data class LocationX(
    val country: String,
    val lat: Double,
    val localtime: String,
    val localtime_epoch: Int,
    val lon: Double,
    val name: String,
    val region: String,
    val tz_id: String
)

data class Hour(
    val temp_c: Double,
    val time: String,
    val condition: ConditionX
)

data class ForecastX(
    val forecastday: List<ForecastdayX>
)

data class ForecastdayX(
    val date: String,
    val hour: List<Hour>
)

data class CurrentX(
    val temp_c: Double,
    val condition: ConditionX,
    val localtime: String
)

data class ConditionX(
    val icon: String,
    val text: String
)

data class AlertsX(
    val alert: List<Any>
)

data class WeatherModel(
    val alerts: AlertsX,
    val current: CurrentX,
    val forecast: ForecastX,
    val location: LocationX
)

// API Key
object Constant {
    val API_KEY = "2514a605f10c4d30b60175346251102"
}

// Retrofit instance setup
object RetrofitInstance {
    private const val BASE_URL = "https://api.weatherapi.com"

    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApi: WeatherApi = getInstance().create(WeatherApi::class.java)
}

// API interface
interface WeatherApi {
    @GET("/v1/forecast.json")
    suspend fun getWeather(
        @Query("key") apiKey: String,
        @Query("q") city: String
    ): Response<WeatherModel>
}
class PermissionHandler(private val activity: ComponentActivity, private val onPermissionGranted: () -> Unit) {

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                // Handle permission denial (e.g., show a message)
            }
        }

    fun requestLocationPermission(activity: ComponentActivity) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
class WeatherViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
fun requestLocation(activity: ComponentActivity) {
    PermissionHandler(activity) {
        // Perform action when permission is granted
        val viewModel = WeatherViewModel(activity)
        viewModel.getCurrentLocationWeather()
    }.requestLocationPermission(activity)
}
// ViewModel for managing weather data
class WeatherViewModel(private val context: Context) : ViewModel() {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _weatherResult = MutableStateFlow<NetworkResponse<WeatherModel>>(NetworkResponse.Loading)
    val weatherResult: StateFlow<NetworkResponse<WeatherModel>> = _weatherResult

    init {
        getCurrentLocationWeather()
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocationWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
                getData("$lat,$lon") // Fetch weather for current location
            } ?: run {
                _weatherResult.value = NetworkResponse.Error("Unable to get location")
            }
        }.addOnFailureListener {
            _weatherResult.value = NetworkResponse.Error("Location services not available")
        }
    }

    fun getData(city: String) {
        _weatherResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(Constant.API_KEY, city)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _weatherResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    _weatherResult.value = NetworkResponse.Error("Failed to Load Data")
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Failed to Load Data")
            }
        }
    }


}



// UI Composable function for Weather Forecast Screen
@Composable
fun WeatherForecastScreen(viewModel: WeatherViewModel) {
    var searchquery by remember { mutableStateOf("") }
    val weatherResult = viewModel.weatherResult
    LaunchedEffect(true) {
        viewModel.getCurrentLocationWeather() // Fetch live location weather
    }
    Box(modifier = Modifier.background(Color(0xFF9FD2FC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search bar
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(13.dp)
                    .background(Color(0xFFEEEEEE), shape = RoundedCornerShape(20.dp)), // Light grey rounded background
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchquery,
                        onValueChange = { searchquery = it },
                        label = { Text("Search") },
                        placeholder = { Text("Enter city name",style = TextStyle(
                            color =  Color(0xFF90A4AE))
                        )},
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor =  Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp)) // Round edges
                    )
                    IconButton(
                        onClick = {
                            if (searchquery.isNotEmpty()) {
                                viewModel.getData(searchquery) // Fetch weather for searched city
                            }

                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3)) // Green circular button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search the city here",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            val weatherResult by viewModel.weatherResult.collectAsState()
            // Handling different network states
            when (weatherResult) {
                is NetworkResponse.Error -> Text(text = (weatherResult as NetworkResponse.Error).message)
                NetworkResponse.Loading -> CircularProgressIndicator(modifier = Modifier.padding(30.dp))
                is NetworkResponse.Success -> WeatherDetails((weatherResult as NetworkResponse.Success).data)
                null -> {}
            }

        }
    }
}

// UI Composable function for displaying weather details
@Composable
fun WeatherDetails(data: WeatherModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF9FD2FC), Color(0xFF5EAFEF)) // Soft blue gradient sky
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weather Icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https:${data.current.condition.icon}")
                    .build(),
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(8.dp)
            )

            // Temperature & Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${data.current.temp_c}℃",
                        style = TextStyle(fontSize = 55.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = "Location", tint = Color(0xFF1565C0))
                        Text(
                            text = "${data.location.name}, ${data.location.country}",
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555))
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(painter = painterResource(id= R.drawable.baseline_access_time_24), contentDescription = "Time", tint = Color(0xFF1565C0))
                        Text(
                            text = " ${data.location.localtime}",
                            style = TextStyle(fontSize = 18.sp, color = Color(0xFF777777))
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(painter = painterResource(id= R.drawable.baseline_cloud_24), contentDescription = "Weather", tint = Color(0xFF1565C0))
                        Text(
                            text = "${data.current.condition.text}",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
                        )
                    }
                }
            }

            // Hourly Forecast Section
            Text(
                text = "Hourly Forecast",
                style = TextStyle(fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )

            // Horizontal Scrollable Forecast
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.forecast.forecastday.firstOrNull()?.hour?.forEach { hour ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier
                            .width(140.dp)
                            .height(230.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Time with Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(painter = painterResource(id=R.drawable.baseline_schedule_24), contentDescription = "Time", tint = Color(0xFF1565C0))
                                Text(
                                    text = hour.time.substring(11), // Extracting time only
                                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                            }

                            // Weather Icon
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https:${hour.condition.icon}")
                                    .build(),
                                contentDescription = "Hourly Weather Icon",
                                modifier = Modifier.size(70.dp)
                            )

                            // Temperature with Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(painter = painterResource(id=R.drawable.baseline_thermostat_24), contentDescription = "Temp", tint = Color(0xFF1565C0))
                                Text(
                                    text = "${hour.temp_c}℃",
                                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                            }

                            // Weather Condition
                            Text(
                                text = hour.condition.text,
                                style = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center, color = Color.DarkGray),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}