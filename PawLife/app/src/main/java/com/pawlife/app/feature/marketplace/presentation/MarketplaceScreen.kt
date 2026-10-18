package com.pawlife.app.feature.marketplace.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity
import com.pawlife.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val allListings by viewModel.listings.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredListings = if (filter == "ALL") {
        allListings
    } else {
        allListings.filter { it.type == filter }
    }.filter { it.title.contains(searchQuery, ignoreCase = true) || it.breed?.contains(searchQuery, ignoreCase = true) == true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pet Marketplace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                        if (cartItems.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .background(Danger, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cartItems.size}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Post listing dialog */ },
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Listing")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search pets, food, toys...", color = SlateSubtle) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SlateSubtle) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = CardDark,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = SlateLight,
                    unfocusedTextColor = SlateLight
                )
            )

            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedFilterChip("All", Icons.Default.Pets, filter == "ALL") { viewModel.setFilter("ALL") }
                AnimatedFilterChip("Adopt", Icons.Default.Favorite, filter == "ADOPT") { viewModel.setFilter("ADOPT") }
                AnimatedFilterChip("Buy Pets", Icons.Default.ShoppingBag, filter == "SELL") { viewModel.setFilter("SELL") }
                AnimatedFilterChip("Food", Icons.Default.Restaurant, filter == "FOOD") { viewModel.setFilter("FOOD") }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredListings) { listing ->
                    PremiumListingCard(
                        listing = listing,
                        onAddToCart = { cartViewModel.addToCart(listing) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedFilterChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (isSelected) Primary else CardDark, label = "bg")
    val textColor by animateColorAsState(if (isSelected) Color.White else SlateSubtle, label = "text")
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .bounceClick { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = text, tint = textColor, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumListingCard(
    listing: MarketplaceListingEntity,
    onAddToCart: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    AsyncImage(
                        model = listing.imageUrl,
                        contentDescription = "Pet Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Favorite Button Floating
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { isFavorite = !isFavorite },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Danger else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Verified Badge
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Secondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateLight, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = SlateSubtle, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("2.4 miles away", style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    
                    if (listing.type == "SELL" || listing.type == "FOOD") {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$${listing.price}", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier.size(32.dp).background(Primary, CircleShape)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        Text("Free Adoption", style = MaterialTheme.typography.titleMedium, color = Success, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
