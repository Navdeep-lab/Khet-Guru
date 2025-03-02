package com.example.khetguru.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// API Constants
const val BASE_URL = "https://api.data.gov.in/resource/"
const val API_KEY = "579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b"

// Data Model
data class MandiPrice(
    var id: String = "",
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val modal_price: String
) {
    // No-arg constructor required by Firebase
    constructor() : this("", "", "", "", "", "")
}


data class MarketPriceResponse(val records: List<MandiPrice>?)

// Retrofit API Service
interface MarketPriceApi {
    @GET("9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getMarketPrices(
        @Query("api-key") apiKey: String = API_KEY,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 100000,
        @Query("offset") offset: Int = 0
    ): MarketPriceResponse
}


// Retrofit Client
object RetrofitClient {
    val apiService: MarketPriceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketPriceApi::class.java)
    }
}

// ViewModel
class MarketPriceViewModel(application: Application) : AndroidViewModel(application) {

    private val _prices = MutableStateFlow<List<MandiPrice>>(emptyList())
    val prices: StateFlow<List<MandiPrice>> = _prices

    private val _savedPrices = MutableStateFlow<List<MandiPrice>>(emptyList())
    val savedPrices: StateFlow<List<MandiPrice>> = _savedPrices

    private val realtimeDbRef = FirebaseDatabase.getInstance().reference.child("saved_mandi_prices")

    var searchQuery by mutableStateOf("")

    init {
        fetchMarketPrices()
        fetchSavedPricesFromRealtimeDB()
    }

    /** Save Price to Firebase Realtime Database */
    fun savePriceToRealtimeDB(mandi: MandiPrice) {
        val mandiId = realtimeDbRef.push().key ?: return
        mandi.id = mandiId // Ensure ID is stored in the object

        realtimeDbRef.child(mandiId).setValue(mandi)
            .addOnSuccessListener { Log.d("RealtimeDB", "Saved successfully: ${mandi.commodity}") }
            .addOnFailureListener { e -> Log.e("RealtimeDB", "Error saving", e) }
    }

    /** Fetch saved prices from Firebase Realtime Database */
    private fun fetchSavedPricesFromRealtimeDB() {
        realtimeDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val prices = snapshot.children.mapNotNull { it.getValue(MandiPrice::class.java) }
                Log.d("RealtimeDB", "Fetched from RealtimeDB: $prices")
                _savedPrices.value = prices
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDB", "Error fetching", error.toException())
            }
        })
    }

    /** Remove Price from Firebase Realtime Database */
    fun removeSavedPrice(mandi: MandiPrice) {
        _savedPrices.value = _savedPrices.value.filterNot { it.id == mandi.id }

        realtimeDbRef.orderByChild("id").equalTo(mandi.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Log.d(
                                    "RealtimeDB",
                                    "Removed: ${mandi.commodity}"
                                )
                            }
                            .addOnFailureListener { e -> Log.e("RealtimeDB", "Error removing", e) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDB", "Error removing", error.toException())
                }
            })
    }

    /** Fetch Market Prices from API */
    fun fetchMarketPrices() {
        viewModelScope.launch {
            val allPrices = mutableListOf<MandiPrice>()
            var offset = 0
            val limit = 1000  // Fetch 1000 records per request
            var hasMoreData = true

            try {
                while (hasMoreData) {
                    val response = RetrofitClient.apiService.getMarketPrices(offset = offset, limit = limit)
                    val prices = response.records ?: emptyList()

                    if (prices.isNotEmpty()) {
                        allPrices.addAll(prices)
                        offset += limit  // Move to the next batch
                        Log.d("API_RESPONSE", "Fetched ${prices.size} records, Total: ${allPrices.size}")
                    } else {
                        hasMoreData = false  // Stop if no more records are available
                    }
                }

                Log.d("API_RESPONSE", "Total fetched records: ${allPrices.size}")
                _prices.value = allPrices
            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to fetch market prices", e)
                _prices.value = emptyList()
            }
        }
    }
}

@Composable
fun SavedPricesScreen(viewModel: MarketPriceViewModel, navController: NavController) {
    val savedPrices by viewModel.savedPrices.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF388E3C))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "📌 Saved Prices",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (savedPrices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved prices found", fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                itemsIndexed(savedPrices) { index, item ->
                    SavedMandiPriceCard(
                        mandi = item,
                        index = index,
                        onRemoveClick = { viewModel.removeSavedPrice(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedMandiPriceCard(mandi: MandiPrice, index: Int, onRemoveClick: () -> Unit) {
    val backgroundColors = listOf(Color(0xFFFFF3CD), Color(0xFFC8E6C9), Color(0xFFBBDEFB))
    val cardColor = backgroundColors[index % backgroundColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = " ${mandi.commodity} - ${mandi.market}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "📍 ${mandi.state}, ${mandi.district}", color = Color.DarkGray)
                Text(
                    text = "💰 Price: ₹${mandi.modal_price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Price",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPriceScreen(viewModel: MarketPriceViewModel, navController: NavController) {
    val prices by viewModel.prices.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredPrices = prices.filter { mandi ->
        mandi.commodity.contains(searchQuery, ignoreCase = true) ||
                mandi.market.contains(searchQuery, ignoreCase = true) ||
                mandi.state.contains(searchQuery, ignoreCase = true) ||
                mandi.district.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Crop or Market", fontWeight = FontWeight.Bold) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("saved_prices") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(54.dp)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "📌 View Saved Prices",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            itemsIndexed(filteredPrices) { index, item ->
                MandiPriceCard(
                    mandi = item,
                    index = index,
                    onSaveClick = { viewModel.savePriceToRealtimeDB(it) }
                )
            }
        }
    }
}

@Composable
fun MandiPriceCard(mandi: MandiPrice, index: Int, onSaveClick: (MandiPrice) -> Unit = {}) {
    val backgroundColors = listOf(Color(0xFFFFCDD2), Color(0xFFC8E6C9), Color(0xFFBBDEFB))
    val cardColor = backgroundColors[index % backgroundColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "${mandi.commodity} - ${mandi.market}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "State: ${mandi.state}, District: ${mandi.district}")
                Text(
                    text = "Price: ₹${mandi.modal_price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Save",
                tint = Color.Gray,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onSaveClick(mandi) }
            )
        }
    }
}

