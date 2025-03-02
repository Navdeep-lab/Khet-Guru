package com.example.khetguru.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface SoilHealthApi {
   @POST("predict")
   suspend fun analyzeSoil(@Body request: SoilHealthRequest): SoilHealthResponse
}

data class SoilHealthRequest(
   val N: Float,
   val P: Float,
   val K: Float,
   val temperature: Float,
   val humidity: Float,
   val ph: Float,
   val rainfall: Float
)

data class SoilHealthResponse(
   val recommended_crop: String
)

fun createRetrofit(): SoilHealthApi {
   val retrofit = Retrofit.Builder()
      .baseUrl("http://127.0.0.1:8000/")
      .client(OkHttpClient())
      .addConverterFactory(GsonConverterFactory.create())
      .build()

   return retrofit.create(SoilHealthApi::class.java)
}

@Composable
fun SoilHealthScreen(context: Context) {
   val scope = rememberCoroutineScope()
   val scrollState = rememberScrollState()
   val focusManager = LocalFocusManager.current
   var nitrogen by remember { mutableStateOf("") }
   var phosphorus by remember { mutableStateOf("") }
   var potassium by remember { mutableStateOf("") }
   var temperature by remember { mutableStateOf("") }
   var humidity by remember { mutableStateOf("") }
   var ph by remember { mutableStateOf("") }
   var rainfall by remember { mutableStateOf("") }
   var predictionResult by remember { mutableStateOf<String?>(null) }

   val soilHealthApi = createRetrofit()

   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(Color(0xFFC8E6C9))
         .verticalScroll(scrollState)
         .imePadding(), // Moves UI when keyboard appears
      contentAlignment = Alignment.Center
   ) {
      Column(
         modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
         horizontalAlignment = Alignment.CenterHorizontally
      ) {
         Text(
            text = "\uD83C\uDF31Soil Health Analysis\uD83D\uDC1E",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center
         )

         Spacer(modifier = Modifier.height(20.dp))

         val inputFields = listOf(
            "N (Nitrogen)" to nitrogen,
            "P (Phosphorus)" to phosphorus,
            "K (Potassium)" to potassium,
            "Temperature (°C)" to temperature,
            "Humidity (%)" to humidity,
            "pH Level" to ph,
            "Rainfall (mm)" to rainfall
         )

         inputFields.forEachIndexed { index, (label, value) ->
            OutlinedTextField(
               value = value,
               onValueChange = {
                  when (index) {
                     0 -> nitrogen = it
                     1 -> phosphorus = it
                     2 -> potassium = it
                     3 -> temperature = it
                     4 -> humidity = it
                     5 -> ph = it
                     6 -> rainfall = it
                  }
               },
               label = { Text(label) },
               modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .focusable(), // Helps handle focus properly
               keyboardOptions = KeyboardOptions.Default.copy(
                  imeAction = ImeAction.Done
               ),
               keyboardActions = KeyboardActions(
                  onDone = { focusManager.clearFocus() }
               )
            )
         }

         Spacer(modifier = Modifier.height(24.dp))

         Button(
            onClick = {
               scope.launch {
                  try {
                     val response = soilHealthApi.analyzeSoil(
                        SoilHealthRequest(
                           nitrogen.toFloat(),
                           phosphorus.toFloat(),
                           potassium.toFloat(),
                           temperature.toFloat(),
                           humidity.toFloat(),
                           ph.toFloat(),
                           rainfall.toFloat()
                        )
                     )
                     predictionResult = "\uD83C\uDF31 Recommended Crop: ${response.recommended_crop}"
                  } catch (e: Exception) {
                     predictionResult = "\u274C Error analyzing soil. Please check inputs and try again."
                  }
               }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF236027)),
            modifier = Modifier
               .shadow(10.dp, shape = RoundedCornerShape(20.dp))
               .height(56.dp)
               .width(220.dp)
         ) {
            Text(
               text = "\uD83D\uDD0D Analyze Soil",
               color = Color.White,
               fontSize = 20.sp,
               fontWeight = FontWeight.Bold
            )
         }

         Spacer(modifier = Modifier.height(24.dp))

         predictionResult?.let {
            Card(
               modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
               colors = CardDefaults.cardColors(containerColor = Color.White),
               elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
               shape = RoundedCornerShape(16.dp)
            ) {
               Text(
                  text = it,
                  modifier = Modifier.padding(20.dp),
                  color = Color(0xFF1B5E20),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Medium,
                  textAlign = TextAlign.Start
               )
            }
         }
      }
   }
}

