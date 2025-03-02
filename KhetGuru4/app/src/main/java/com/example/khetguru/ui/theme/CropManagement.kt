package com.example.khetguru.ui.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.util.Base64
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import kotlin.io.encoding.ExperimentalEncodingApi

// Function to Convert URI to File
fun getFileFromUri(context: Context, uri: Uri): File? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return inputStream?.let {
        val file = File(context.cacheDir, "uploaded_image.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        file
    }
}

fun formatApiResponse(response: String): String {
    return try {
        val jsonObject = JSONObject(response)
        val resultObject = jsonObject.optJSONObject("result")

        // Extract plant details
        val cropSuggestion = resultObject?.optJSONObject("crop")?.optJSONArray("suggestions")
        val plantName = cropSuggestion?.optJSONObject(0)?.optString("name", "Unknown Plant") ?: "Unknown Plant"
        val scientificName = cropSuggestion?.optJSONObject(0)?.optString("scientific_name", "N/A") ?: "N/A"

        // Extract disease details
        val diseaseArray = resultObject?.optJSONObject("disease")?.optJSONArray("suggestions")
        val diseaseInfoList = mutableListOf<String>()

        if (diseaseArray != null && diseaseArray.length() > 0) {
            for (i in 0 until diseaseArray.length()) {
                val disease = diseaseArray.optJSONObject(i)
                val diseaseName = disease?.optString("name", "Unknown Disease") ?: "Unknown Disease"
                val probability = disease?.optDouble("probability", 0.0)?.times(100)?.toInt() ?: 0
                val scientificDiseaseName = disease?.optString("scientific_name", "N/A") ?: "N/A"

                val preventionList = disease?.optJSONObject("details")?.optJSONObject("treatment")?.optJSONArray("prevention")
                val prevention = preventionList?.let {
                    if (it.length() > 0) "\n\uD83D\uDD34 \uD835\uDDE3\uD835\uDDE5\uD835\uDDD8\uD835\uDDE9\uD835\uDDD8\uD835\uDDE1\uD835\uDDE7\uD835\uDDDC\uD835\uDDE2\uD835\uDDE1: \n\uD83D\uDD39 " +  it.optString(0) else ""
                } ?: ""
                val chemicalTreatmentList = disease?.optJSONObject("details")?.optJSONObject("treatment")?.optJSONArray("chemical treatment")
                val chemicalTreatment = chemicalTreatmentList?.let {
                    if (it.length() > 0) "\n\uD83E\uDDEA \uD835\uDDD6\uD835\uDDDB\uD835\uDDD8\uD835\uDDE0\uD835\uDDDC\uD835\uDDD6\uD835\uDDD4\uD835\uDDDF \uD835\uDDE7\uD835\uDDE5\uD835\uDDD8\uD835\uDDD4\uD835\uDDE7\uD835\uDDE0\uD835\uDDD8\uD835\uDDE1\uD835\uDDE7: \n\uD83D\uDD39 " +  it.optString(0) else ""
                } ?: ""

                val biologicalTreatmentList = disease?.optJSONObject("details")?.optJSONObject("treatment")?.optJSONArray("biological treatment")
                val biologicalTreatment = biologicalTreatmentList?.let {
                    if (it.length() > 0) "\n\uD83C\uDF31 \uD835\uDDD5\uD835\uDDDC\uD835\uDDE2\uD835\uDDDF\uD835\uDDE2\uD835\uDDDA\uD835\uDDDC\uD835\uDDD6\uD835\uDDD4\uD835\uDDDF \uD835\uDDE7\uD835\uDDE5\uD835\uDDD8\uD835\uDDD4\uD835\uDDE7\uD835\uDDE0\uD835\uDDD8\uD835\uDDE1\uD835\uDDE7: \n\uD83D\uDD39 " +  it.optString(0) else ""
                } ?: ""

                diseaseInfoList.add("\n" +
                        "\uD83E\uDDA0 \uD835\uDDD7\uD835\uDDDC\uD835\uDDE6\uD835\uDDD8\uD835\uDDD4\uD835\uDDE6\uD835\uDDD8: $diseaseName ($scientificDiseaseName) \n\uD83C\uDFAF \uD835\uDDD6\uD835\uDDE2\uD835\uDDE1\uD835\uDDD9\uD835\uDDDC\uD835\uDDD7\uD835\uDDD8\uD835\uDDE1\uD835\uDDD6\uD835\uDDD8: $probability% \n $prevention$chemicalTreatment$biologicalTreatment")
            }
        } else {
            diseaseInfoList.add("🦠 🅽🅾 🅳🅸🆂🅴🅰🆂🅴 🅳🅴🆃🅴🅲🆃🅴🅳 - Confidence: *0%*")
        }

        """
        🌱 𝗣𝗟𝗔𝗡𝗧 𝗡𝗔𝗠𝗘: $plantName ($scientificName)
        ${diseaseInfoList.joinToString("\n")}
        """.trimIndent()
    } catch (e: Exception) {
        "❌ 🅴🆁🆁🅾🆁: ${e.localizedMessage}"
    }
}







@OptIn(ExperimentalEncodingApi::class)
@Composable
fun CropIdentificationScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var predictionResult by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    suspend fun analyzeCropWithOkHttp(imageUri: Uri) {
        val file = getFileFromUri(context, imageUri)
        if (file == null) {
            predictionResult = "Error: Unable to access image file."
            return
        }

        // Convert Image to Base64
        val base64Image = try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP) // NO_WRAP removes unnecessary new lines
        } catch (e: Exception) {
            predictionResult = "Error: Unable to encode image."
            return
        }

        val apiKey = "RYUpxEtOoQUlzINbV10fuyTJucuSGRkm1ruolFsfqT0SowvW62"
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val DecodeImage = "data:image/jpeg;base64,$base64Image"

        val body =
            "{\n \"images\": [\n \"$DecodeImage\"\n ],\n \"latitude\": 49.207,\n \"longitude\": 16.608,\n \"similar_images\": true\n}".toRequestBody(
                mediaType
            )

        val request = Request.Builder()
            .url("https://crop.kindwise.com/api/v1/identification?details=common_names,type,taxonomy,eppo_code,eppo_regulation_status,gbif_id,image,images,wiki_url,wiki_description,treatment,description,symptoms,severity,spreading&language=en\n")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Api-Key", apiKey)
            .build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "No Response"
                    Log.d("API Response", responseBody)
                    val formattedResult = formatApiResponse(responseBody)

                    withContext(Dispatchers.Main) {
                        predictionResult = formattedResult
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        predictionResult = "Server Error: ${response.code}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    predictionResult = "Network Error: ${e.message}"
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFC8E6C9)
            )
            .padding(5.dp), // Adds breathing space
        contentAlignment = Alignment.Center
    )
    {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌾Crop Disease Identification🌾",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0x66000000),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )


            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(250.dp)
                    .shadow(10.dp, shape = CircleShape)
                    .border(2.dp, Color(0xFF2E7D32), shape = CircleShape) // Green border
                    .background(Color.White, shape = CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            )
            {
                if (imageUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = "Upload Image",
                            tint = Color.Gray,
                            modifier = Modifier.size(70.dp)
                        )
                        Text(
                            text = "Tap to Upload",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Uploaded Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    imageUri?.let { uri ->
                        predictionResult = "🔍 Analyzing..."
                        coroutineScope.launch {
                            analyzeCropWithOkHttp(uri)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                modifier = Modifier
                    .shadow(12.dp, shape = RoundedCornerShape(16.dp))
                    .height(56.dp)
                    .width(250.dp)
            ) {
                Text(
                    text = "🌾 Identify Disease",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            predictionResult?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 Diagnosis Result",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = it,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun CropManagement(navController: NavController) {
    CropIdentificationScreen()
}
