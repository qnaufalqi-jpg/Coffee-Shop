package com.example.cafecoffe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

sealed class Screen {
    object Home : Screen()
    data class Detail(val coffee: CoffeeItem) : Screen()
    object Favorites : Screen()
    object Notifications : Screen()
    object Cart : Screen()
    object Success : Screen()
}

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = currentScreen is Screen.Home,
            onClick = { onNavigate(Screen.Home) }
        )
        NavigationBarItem(
            icon = { Icon(if (currentScreen is Screen.Favorites) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = "Favorite") },
            selected = currentScreen is Screen.Favorites,
            onClick = { onNavigate(Screen.Favorites) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = "Cart") },
            selected = currentScreen is Screen.Cart,
            onClick = { onNavigate(Screen.Cart) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Notifications") },
            selected = currentScreen is Screen.Notifications,
            onClick = { onNavigate(Screen.Notifications) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var cartItemsByCoffeeId by remember { mutableStateOf(mapOf<Int, Int>()) } // coffeeId -> quantity
    var favoriteCoffeeIds by remember { mutableStateOf(setOf<Int>()) }

    val onNavigate: (Screen) -> Unit = { screen ->
        currentScreen = screen
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFC67C4E),
            onPrimary = Color.White,
            secondary = Color(0xFFED9D59),
            background = Color(0xFFF9F9F9),
            surface = Color.White,
            onSurface = Color(0xFF2F2D2C)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            onCoffeeClick = { coffee -> currentScreen = Screen.Detail(coffee) },
                            onNavigate = onNavigate
                        )
                    }
                    is Screen.Favorites -> {
                        FavoritesScreen(
                            favoriteCoffeeIds = favoriteCoffeeIds,
                            onCoffeeClick = { coffee -> currentScreen = Screen.Detail(coffee) },
                            onNavigate = onNavigate,
                            onToggleFavorite = { id ->
                                favoriteCoffeeIds = favoriteCoffeeIds - id
                            }
                        )
                    }
                    is Screen.Notifications -> {
                        NotificationsScreen(
                            onNavigate = onNavigate
                        )
                    }
                    is Screen.Detail -> {
                        DetailScreen(
                            coffee = screen.coffee,
                            isFavorite = favoriteCoffeeIds.contains(screen.coffee.id),
                            onBackClick = { currentScreen = Screen.Home },
                            onAddToCart = { coffee ->
                                val currentCount = cartItemsByCoffeeId[coffee.id] ?: 0
                                cartItemsByCoffeeId = cartItemsByCoffeeId + (coffee.id to (currentCount + 1))
                                currentScreen = Screen.Cart
                            },
                            onToggleFavorite = {
                                val coffeeId = screen.coffee.id
                                favoriteCoffeeIds = if (favoriteCoffeeIds.contains(coffeeId)) {
                                    favoriteCoffeeIds - coffeeId
                                } else {
                                    favoriteCoffeeIds + coffeeId
                                }
                            }
                        )
                    }
                    is Screen.Cart -> {
                        CartScreen(
                            cartItems = cartItemsByCoffeeId,
                            onBackClick = { currentScreen = Screen.Home },
                            onNavigate = onNavigate,
                            onCheckout = {
                                cartItemsByCoffeeId = emptyMap()
                                currentScreen = Screen.Success
                            },
                            onUpdateQuantity = { id, qty ->
                                if (qty <= 0) {
                                    cartItemsByCoffeeId = cartItemsByCoffeeId - id
                                } else {
                                    cartItemsByCoffeeId = cartItemsByCoffeeId + (id to qty)
                                }
                            }
                        )
                    }
                    is Screen.Success -> {
                        SuccessScreen(
                            onBackHome = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onCoffeeClick: (CoffeeItem) -> Unit,
    onNavigate: (Screen) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All Coffee") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCoffees = sampleCoffees.filter { coffee ->
        val matchesCategory = selectedCategory == "All Coffee" || coffee.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = coffee.name.contains(searchQuery, ignoreCase = true) || coffee.subtitle.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = Screen.Home, onNavigate = onNavigate)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9)),
            contentPadding = PaddingBottomWindow
        ) {
            // Header / Location
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF131313), Color(0xFF313131))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Location",
                                    color = Color(0xFFB7B7B7),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Bilzen, Tanjungbalai",
                                        color = Color(0xFFDDDDDD),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFFDDDDDD),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF202020)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(text = "Search coffee", color = Color(0xFF989898))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFC67C4E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                disabledContainerColor = Color(0xFF2A2A2A),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Promo Banner
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFC67C4E), Color(0xFFED9D59))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFED5151)
                        ) {
                            Text(
                                text = "Promo",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Buy one get one FREE",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "On all coffee drinks today!",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Categories
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            modifier = Modifier.clickable { selectedCategory = category },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFC67C4E) else Color(0xFFEDEDED)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (isSelected) Color.White else Color(0xFF2F2D2C),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Coffee Grid
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    if (filteredCoffees.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No coffee found", color = Color.Gray)
                        }
                    } else {
                        for (rowCoffees in filteredCoffees.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CoffeeCard(
                                    coffee = rowCoffees[0],
                                    modifier = Modifier.weight(1f),
                                    onClick = { onCoffeeClick(rowCoffees[0]) }
                                )
                                if (rowCoffees.size > 1) {
                                    CoffeeCard(
                                        coffee = rowCoffees[1],
                                        modifier = Modifier.weight(1f),
                                        onClick = { onCoffeeClick(rowCoffees[1]) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

val PaddingBottomWindow = PaddingValues(bottom = 32.dp)

@Composable
fun CoffeeCard(
    coffee: CoffeeItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(coffee.colorRes)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Coffee,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${coffee.rating}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = coffee.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2F2D2C),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = coffee.subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9B9B9B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ${String.format(Locale.US, "%.2f", coffee.price)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2F2D2C)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFC67C4E))
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    favoriteCoffeeIds: Set<Int>,
    onCoffeeClick: (CoffeeItem) -> Unit,
    onNavigate: (Screen) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    val favoriteCoffees = sampleCoffees.filter { favoriteCoffeeIds.contains(it.id) }

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = Screen.Favorites, onNavigate = onNavigate)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
                .padding(24.dp)
        ) {
            Text(
                text = "Favorite Coffees",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2F2D2C)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (favoriteCoffees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFC67C4E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No favorites yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF2F2D2C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the heart icon on any coffee to add to favorites!",
                            fontSize = 14.sp,
                            color = Color(0xFF9B9B9B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteCoffees) { coffee ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCoffeeClick(coffee) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(coffee.colorRes)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Coffee,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = coffee.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2F2D2C)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = coffee.subtitle,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9B9B9B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%.2f", coffee.price)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFC67C4E)
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(coffee.id) }) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = "Remove Favorite",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    onNavigate: (Screen) -> Unit
) {
    val notifications = listOf(
        Pair("Order Dispatched", "Your fresh coffee order #1024 is on its way!"),
        Pair("Special Offer 🎉", "Get 20% off on all Macchiato drinks this weekend."),
        Pair("Welcome to CafeCoffe!", "Explore our menu and order your favorite coffee anytime.")
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = Screen.Notifications, onNavigate = onNavigate)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
                .padding(24.dp)
        ) {
            Text(
                text = "Notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2F2D2C)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { (title, message) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF2F2D2C)
                                )
                                Icon(
                                    Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFFC67C4E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message,
                                fontSize = 14.sp,
                                color = Color(0xFF9B9B9B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(
    coffee: CoffeeItem,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onAddToCart: (CoffeeItem) -> Unit,
    onToggleFavorite: () -> Unit
) {
    var selectedSize by remember { mutableStateOf("M") }
    var readMore by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Price",
                            color = Color(0xFF9B9B9B),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$ ${String.format(Locale.US, "%.2f", coffee.price)}",
                            color = Color(0xFFC67C4E),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onAddToCart(coffee) },
                        modifier = Modifier
                            .height(56.dp)
                            .width(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E))
                    ) {
                        Text(
                            text = "Buy Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Detail",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2F2D2C)
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color(0xFF2F2D2C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coffee Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(coffee.colorRes)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Coffee,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Subtitle & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = coffee.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F2D2C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = coffee.subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF9B9B9B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${coffee.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF2F2D2C)
                    )
                    Text(
                        text = " (${coffee.reviewsCount})",
                        fontSize = 12.sp,
                        color = Color(0xFF9B9B9B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEAEAEA))
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2F2D2C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = coffee.description,
                fontSize = 14.sp,
                color = Color(0xFF9B9B9B),
                maxLines = if (readMore) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = { readMore = !readMore }) {
                Text(
                    text = if (readMore) "Read Less" else "Read More",
                    color = Color(0xFFC67C4E),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Size Selector
            Text(
                text = "Size",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2F2D2C)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("S", "M", "L").forEach { size ->
                    val isSelected = size == selectedSize
                    OutlinedButton(
                        onClick = { selectedSize = size },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFFFF5EE) else Color.White,
                            contentColor = if (isSelected) Color(0xFFC67C4E) else Color(0xFF2F2D2C)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFC67C4E) else Color(0xFFDEDEDE)
                        )
                    ) {
                        Text(
                            text = size,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    cartItems: Map<Int, Int>,
    onBackClick: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onCheckout: () -> Unit,
    onUpdateQuantity: (Int, Int) -> Unit
) {
    val itemsInCart = cartItems.mapNotNull { (id, qty) ->
        sampleCoffees.find { it.id == id }?.let { it to qty }
    }

    val subtotal = itemsInCart.sumOf { (coffee, qty) -> coffee.price * qty }
    val deliveryFee = if (itemsInCart.isNotEmpty()) 1.00 else 0.00
    val total = subtotal + deliveryFee

    Scaffold(
        bottomBar = {
            Column {
                if (itemsInCart.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", color = Color(0xFF9B9B9B), fontSize = 14.sp)
                                Text("$ ${String.format(Locale.US, "%.2f", subtotal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee", color = Color(0xFF9B9B9B), fontSize = 14.sp)
                                Text("$ ${String.format(Locale.US, "%.2f", deliveryFee)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2F2D2C))
                                Text("$ ${String.format(Locale.US, "%.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFC67C4E))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCheckout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E))
                            ) {
                                Text(
                                    text = "Order / Checkout",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                AppBottomBar(currentScreen = Screen.Cart, onNavigate = onNavigate)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "My Cart",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2F2D2C)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (itemsInCart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFC67C4E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your cart is empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF2F2D2C)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add some delicious coffee to get started!",
                            fontSize = 14.sp,
                            color = Color(0xFF9B9B9B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsInCart) { (coffee, qty) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(coffee.colorRes)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Coffee,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = coffee.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF2F2D2C)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = coffee.subtitle,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9B9B9B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%.2f", coffee.price)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFC67C4E)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onUpdateQuantity(coffee.id, qty - 1) }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        text = "$qty",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(onClick = { onUpdateQuantity(coffee.id, qty + 1) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessScreen(
    onBackHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC67C4E).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFC67C4E),
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Order Successful!",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF2F2D2C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your fresh coffee is being prepared and will be delivered shortly.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color(0xFF9B9B9B)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC67C4E))
            ) {
                Text(
                    text = "Back to Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
