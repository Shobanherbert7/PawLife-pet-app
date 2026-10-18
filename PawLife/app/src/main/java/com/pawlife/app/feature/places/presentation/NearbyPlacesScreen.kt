package com.pawlife.app.feature.places.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.Warning

data class PetPlace(
    val id: String,
    val name: String,
    val category: String,
    val distanceKm: Float,
    val address: String,
    val phone: String,
    val rating: Float,
    val is24Hours: Boolean,
    val icon: ImageVector,
    val queryAddress: String
)

@Composable
fun NearbyPlacesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val places = remember {
        listOf(
            PetPlace(
                id = "p1",
                name = "Anna Nagar 24/7 Veterinary Hospital",
                category = "Emergency Vet",
                distanceKm = 1.4f,
                address = "2nd Avenue, Anna Nagar, Chennai",
                phone = "+919840122334",
                rating = 4.9f,
                is24Hours = true,
                icon = Icons.Default.LocalHospital,
                queryAddress = "Anna Nagar Veterinary Hospital Chennai"
            ),
            PetPlace(
                id = "p2",
                name = "Semmozhi Poonga Dog Trail & Park",
                category = "Dog Park",
                distanceKm = 2.8f,
                address = "Cathedral Road, Teynampet, Chennai",
                phone = "+919840155667",
                rating = 4.8f,
                is24Hours = false,
                icon = Icons.Default.Park,
                queryAddress = "Semmozhi Poonga Chennai"
            ),
            PetPlace(
                id = "p3",
                name = "Barking Deer Pet Cafe & Play Zone",
                category = "Pet Cafe",
                distanceKm = 3.2f,
                address = "Khadar Nawaz Khan Rd, Nungambakkam",
                phone = "+919840188990",
                rating = 4.7f,
                is24Hours = false,
                icon = Icons.Default.Restaurant,
                queryAddress = "Pet Cafe Nungambakkam Chennai"
            ),
            PetPlace(
                id = "p4",
                name = "Madras Veterinary College Emergency Clinic",
                category = "Emergency Vet",
                distanceKm = 4.1f,
                address = "Vepery High Road, Vepery, Chennai",
                phone = "+914425304000",
                rating = 4.9f,
                is24Hours = true,
                icon = Icons.Default.LocalHospital,
                queryAddress = "Madras Veterinary College Vepery Chennai"
            ),
            PetPlace(
                id = "p5",
                name = "Besant Nagar Beach Dog Run Zone",
                category = "Dog Park",
                distanceKm = 7.5f,
                address = "6th Avenue, Elliot's Beach, Besant Nagar",
                phone = "+919840199112",
                rating = 4.8f,
                is24Hours = false,
                icon = Icons.Default.Park,
                queryAddress = "Elliots Beach Besant Nagar Chennai"
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Emergency Vet", "Dog Park", "Pet Cafe")

    val filteredPlaces = remember(selectedCategory) {
        if (selectedCategory == "All") places else places.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(top = 44.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF151C2C), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column {
                    Text(
                        text = "Nearby Parks & 24/7 Vets",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chennai • 5 verified destinations",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Primary else Color(0xFF151C2C),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Primary else Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = if (cat == "Emergency Vet") "🚨 24/7 Vets" else if (cat == "Dog Park") "🌳 Dog Parks" else if (cat == "Pet Cafe") "☕ Pet Cafes" else "All Places",
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Places List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredPlaces) { place ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF151C2C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (place.is24Hours) Color(0xFFEF4444).copy(alpha = 0.2f) else Primary.copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = place.icon,
                                        contentDescription = null,
                                        tint = if (place.is24Hours) Color(0xFFEF4444) else Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = place.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = place.category,
                                            color = if (place.is24Hours) Color(0xFFEF4444) else Secondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text("•", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                                        Text(
                                            text = "${place.distanceKm} km away",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            // Rating Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Warning.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Warning, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = "${place.rating}",
                                        color = Warning,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = place.address,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Call & Navigate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${place.phone}")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(place.queryAddress)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // Fallback to web browser maps
                                        val webMap = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(place.queryAddress)}"))
                                        context.startActivity(webMap)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Navigate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
