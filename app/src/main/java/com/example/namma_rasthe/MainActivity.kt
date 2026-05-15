package com.example.namma_rasthe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.namma_rasthe.ui.theme.*
import kotlinx.coroutines.delay

// --- Navigation Enum ---
enum class Screen {
    SPLASH, LOGIN, REGISTER, REGION, HOME, REPORT, TRACKER, HISTORY, CONFIRM
}

// --- Data Models ---
data class Region(val id: Int, val state: String, val districts: List<String>)

data class IssueType(val id: String, val label: String, val icon: String, val color: Color)

data class Report(
    val ticketId: String,
    val type: String,
    val status: String,
    val date: String,
    val address: String,
    val severity: String,
    val description: String = "",
    val photoTaken: Boolean = false,
    val latitude: String = "12.9716° N",
    val longitude: String = "77.5946° E"
)

// --- Constants ---
val REGIONS = listOf(
    Region(1, "Karnataka", listOf("Bengaluru Urban", "Mysuru", "Hubli-Dharwad", "Mangaluru", "Belagavi")),
    Region(2, "Tamil Nadu", listOf("Chennai", "Coimbatore", "Madurai", "Salem", "Tiruchirappalli")),
    Region(3, "Maharashtra", listOf("Mumbai", "Pune", "Nagpur", "Nashik", "Aurangabad")),
    Region(4, "Delhi", listOf("New Delhi", "North Delhi", "South Delhi", "East Delhi", "West Delhi")),
    Region(5, "Telangana", listOf("Hyderabad", "Warangal", "Nizamabad", "Karimnagar", "Khammam"))
)

val ISSUE_TYPES = listOf(
    IssueType("pothole", "Pothole", "🕳️", Color(0xFFE53E3E)),
    IssueType("streetlight", "Broken Light", "💡", Color(0xFFD69E2E)),
    IssueType("road_damage", "Road Damage", "🛣️", Color(0xFFDD6B20)),
    IssueType("corridor", "Corridor", "🏗️", Color(0xFF6B46C1)),
    IssueType("drain", "Open Drain", "🌊", Color(0xFF2B6CB0)),
    IssueType("garbage", "Garbage", "🗑️", Color(0xFF276749))
)

val SEVERITY_LIST = listOf("Low", "Medium", "High", "Critical")

val MOCK_REPORTS = mutableStateListOf(
    Report("NR-M3K2P-A4X", "Pothole", "In Progress", "09 May, 2026", "MG Road, Bengaluru", "High"),
    Report("NR-M3J9Q-B7Y", "Broken Light", "Resolved", "05 May, 2026", "Koramangala, Bengaluru", "Medium"),
    Report("NR-M3I8R-C2Z", "Road Damage", "Pending", "01 May, 2026", "Whitefield, Bengaluru", "Critical")
)

fun generateTicketId(): String {
    val prefix = "NR"
    val ts = System.currentTimeMillis().toString(36).uppercase()
    val rand = (100..999).random().toString()
    return "$prefix-$ts-$rand"
}

// --- App Entry ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Namma_rastheTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var user by remember { mutableStateOf<String?>(null) }
    var selectedRegion by remember { mutableStateOf<Region?>(null) }
    var selectedDistrict by remember { mutableStateOf<String?>(null) }
    var lastTicket by remember { mutableStateOf<Report?>(null) }
    var navActive by remember { mutableStateOf(Screen.HOME) }

    val showBottomBar = currentScreen in listOf(Screen.HOME, Screen.TRACKER, Screen.HISTORY)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    activeScreen = navActive,
                    onNavigate = { screen ->
                        navActive = screen
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
                when (screen) {
                    Screen.SPLASH -> SplashScreen { currentScreen = Screen.LOGIN }
                    Screen.LOGIN -> LoginScreen(
                        onLogin = { email ->
                            user = email.substringBefore("@")
                            currentScreen = Screen.REGION
                        },
                        onRegisterClick = { currentScreen = Screen.REGISTER }
                    )
                    Screen.REGISTER -> RegisterScreen(
                        onRegister = { name ->
                            user = name
                            currentScreen = Screen.REGION
                        },
                        onBackToLogin = { currentScreen = Screen.LOGIN }
                    )
                    Screen.REGION -> RegionScreen(
                        selectedRegion = selectedRegion,
                        selectedDistrict = selectedDistrict,
                        onRegionSelect = { selectedRegion = it },
                        onDistrictSelect = { selectedDistrict = it },
                        onConfirm = { currentScreen = Screen.HOME }
                    )
                    Screen.HOME -> HomeScreen(
                        userName = user ?: "Citizen",
                        district = selectedDistrict ?: "Unknown",
                        state = selectedRegion?.state ?: "Unknown",
                        onReportClick = { currentScreen = Screen.REPORT },
                        onTrackClick = {
                            navActive = Screen.TRACKER
                            currentScreen = Screen.TRACKER
                        },
                        onHistoryClick = {
                            navActive = Screen.HISTORY
                            currentScreen = Screen.HISTORY
                        }
                    )
                    Screen.REPORT -> ReportScreen(
                        onBack = { currentScreen = Screen.HOME },
                        onSubmit = { report ->
                            lastTicket = report
                            MOCK_REPORTS.add(0, report)
                            currentScreen = Screen.CONFIRM
                        }
                    )
                    Screen.TRACKER -> TrackerScreen(onBack = { 
                        navActive = Screen.HOME
                        currentScreen = Screen.HOME 
                    })
                    Screen.HISTORY -> HistoryScreen(onBack = { 
                        navActive = Screen.HOME
                        currentScreen = Screen.HOME 
                    })
                    Screen.CONFIRM -> ConfirmScreen(report = lastTicket) {
                        navActive = Screen.HOME
                        currentScreen = Screen.HOME
                    }
                }
            }
        }
    }
}

// --- Components ---

@Composable
fun BottomNavigationBar(activeScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(64.dp)
    ) {
        NavigationBarItem(
            selected = activeScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary, unselectedIconColor = Muted, indicatorColor = AccentLight
            )
        )
        NavigationBarItem(
            selected = activeScreen == Screen.TRACKER,
            onClick = { onNavigate(Screen.TRACKER) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Track") },
            label = { Text("Track", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary, unselectedIconColor = Muted, indicatorColor = AccentLight
            )
        )
        NavigationBarItem(
            selected = activeScreen == Screen.HISTORY,
            onClick = { onNavigate(Screen.HISTORY) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
            label = { Text("Reports", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary, unselectedIconColor = Muted, indicatorColor = AccentLight
            )
        )
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(2200)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GradBot, GradTop, Green))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(90.dp).scale(scale).background(Accent, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
                Text("🛣️", fontSize = 44.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Namma-Raste", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("INFRASTRUCTURE REPORTER", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(40.dp))
            Text("Powered by GenAI · MindMatrix VTU", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradBot, GradTop)), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 48.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Accent, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("🛣️", fontSize = 20.sp) }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Namma-Raste", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("INFRASTRUCTURE REPORTER", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Welcome Back 👋", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Login to report road issues in your area", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
        Column(modifier = Modifier.padding(28.dp)) {
            Text("EMAIL ADDRESS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("your@email.com") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0F4F8)))
            Spacer(modifier = Modifier.height(16.dp))
            Text("PASSWORD", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Enter your password") }, shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF0F4F8)))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { if(email.isNotBlank()) onLogin(email) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("LOGIN →", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Don't have an account? Register Here", modifier = Modifier.fillMaxWidth().clickable { onRegisterClick() }, textAlign = TextAlign.Center, fontSize = 13.sp, color = Accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RegisterScreen(onRegister: (String) -> Unit, onBackToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradBot, GradTop)), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(28.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBackToLogin() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Login", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Create Account", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Join thousands of civic reporters", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        LazyColumn(modifier = Modifier.padding(24.dp)) {
            item {
                Text("FULL NAME", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("EMAIL ADDRESS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("PHONE NUMBER", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("PASSWORD", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { if(name.isNotBlank()) onRegister(name) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("CREATE ACCOUNT →", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun RegionScreen(selectedRegion: Region?, selectedDistrict: String?, onRegionSelect: (Region) -> Unit, onDistrictSelect: (String) -> Unit, onConfirm: () -> Unit) {
    var expandedStateId by remember { mutableStateOf<Int?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradBot, GradTop)), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).padding(28.dp)) {
            Column {
                Text("Select Your Region 📍", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Reports go to your local municipal office", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                if (selectedRegion != null && selectedDistrict != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
                        Text("📌 $selectedDistrict, ${selectedRegion.state}", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(20.dp)) {
            item { Text("CHOOSE STATE & DISTRICT", color = Dark, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold); Spacer(modifier = Modifier.height(12.dp)) }
            items(REGIONS) { region ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().clickable { expandedStateId = if (expandedStateId == region.id) null else region.id }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🏛️ ${region.state}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Icon(if (expandedStateId == region.id) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Muted)
                        }
                        AnimatedVisibility(visible = expandedStateId == region.id) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                region.districts.forEach { district ->
                                    val isSelected = selectedDistrict == district && selectedRegion?.id == region.id
                                    Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable { onRegionSelect(region); onDistrictSelect(district) }, color = if (isSelected) Primary else Color(0xFFF0F4F8), shape = RoundedCornerShape(10.dp)) {
                                        Text(text = (if (isSelected) "✓ " else "○ ") + district, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), color = if (isSelected) Color.White else Dark, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Button(onClick = onConfirm, enabled = selectedRegion != null && selectedDistrict != null, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("CONFIRM REGION →", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun HomeScreen(userName: String, district: String, state: String, onReportClick: () -> Unit, onTrackClick: () -> Unit, onHistoryClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradBot, GradTop))).padding(20.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Good morning,", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text("$userName 👋", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍", fontSize = 12.sp); Spacer(modifier = Modifier.width(6.dp))
                            Text("$district, $state", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.size(42.dp).background(Accent, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = Dark) }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    HomeStat("3", "Reports Sent"); HomeStat("1", "In Progress"); HomeStat("1", "Resolved")
                }
            }
        }
        LazyColumn(modifier = Modifier.padding(20.dp)) {
            item {
                Button(onClick = onReportClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Dark)) {
                    Text("📷  REPORT AN ISSUE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeActionCard("🔍 Track", "Report", "Enter Ticket ID", Primary, Modifier.weight(1f), onClick = onTrackClick)
                    HomeActionCard("📋 My", "Reports", "View History", Green, Modifier.weight(1f), onClick = onHistoryClick)
                }
            }
        }
    }
}

@Composable
fun HomeStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}

@Composable
fun HomeActionCard(t1: String, t2: String, sub: String, bg: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(t1, color = Color.White, fontSize = 22.sp); Text(t2, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Text(sub, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
fun ReportScreen(onBack: () -> Unit, onSubmit: (Report) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var selectedType by remember { mutableStateOf<IssueType?>(null) }
    var severity by remember { mutableStateOf("Medium") }
    var description by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("12.9716° N") }
    var lng by remember { mutableStateOf("77.5946° E") }
    var photoTaken by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBar(title = if (step == 1) "Issue Type" else "Details & Location", onBack = { if (step == 1) onBack() else step = 1 })
        Column(modifier = Modifier.weight(1f).padding(20.dp)) {
            if (step == 1) {
                Text("Select the type of infrastructure issue:", color = Dark, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ISSUE_TYPES) { type ->
                        val isSelected = selectedType?.id == type.id
                        Card(modifier = Modifier.fillMaxWidth().clickable { selectedType = type }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) type.color.copy(alpha = 0.1f) else CardBackground), border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, type.color) else null) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(type.icon, fontSize = 32.sp); Spacer(modifier = Modifier.height(8.dp)); Text(type.label, fontWeight = FontWeight.Bold, color = Dark, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            } else {
                Text("Severity Level", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SEVERITY_LIST.forEach { s ->
                        val isSelected = severity == s
                        Surface(modifier = Modifier.weight(1f).clickable { severity = s }, color = if (isSelected) Primary else Color(0xFFF0F4F8), shape = RoundedCornerShape(10.dp)) {
                            Text(s, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, color = if (isSelected) Color.White else Dark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)); Text("Description", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth().height(100.dp), placeholder = { Text("Describe the issue...") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White))
                Spacer(modifier = Modifier.height(20.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(if (photoTaken) Green.copy(alpha = 0.1f) else Color(0xFFF0F4F8), CircleShape), contentAlignment = Alignment.Center) { Icon(if (photoTaken) Icons.Default.Check else Icons.Default.CameraAlt, contentDescription = null, tint = if (photoTaken) Green else Muted) }
                        Spacer(modifier = Modifier.width(16.dp)); Column { Text("Take Issue Photo", fontWeight = FontWeight.Bold, color = Dark); Text(if (photoTaken) "Photo captured" else "Required", fontSize = 11.sp, color = Muted) }
                        Spacer(modifier = Modifier.weight(1f))
                        if (isCapturing) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Primary)
                        else TextButton(onClick = { isCapturing = true }) { Text(if (photoTaken) "RETAKE" else "CAPTURE", color = Primary, fontWeight = FontWeight.Black) }
                    }
                    LaunchedEffect(isCapturing) { if (isCapturing) { delay(1000); photoTaken = true; isCapturing = false } }
                }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AccentLight)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Accent); Spacer(modifier = Modifier.width(8.dp)); Text("Auto-detect Location", fontWeight = FontWeight.Bold, color = Dark); Spacer(modifier = Modifier.weight(1f))
                            if (isLocating) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Accent)
                            else TextButton(onClick = { isLocating = true }) { Text("DETECT", color = Accent, fontWeight = FontWeight.Black) }
                        }
                        LaunchedEffect(isLocating) { if (isLocating) { delay(1500); val i = (0..3).random(); address = listOf("MG Road, Bengaluru", "Koramangala 5th Block", "Whitefield Main Rd", "Indiranagar 100ft Rd")[i]; lat = listOf("12.9716° N", "13.0827° N", "12.8456° N", "12.9352° N")[i]; lng = listOf("77.5946° E", "77.6033° E", "77.6784° E", "77.6245° E")[i]; isLocating = false } }
                        if (address.isNotEmpty()) { Text(address, fontSize = 13.sp, color = Muted, modifier = Modifier.padding(start = 32.dp)); Text("$lat, $lng", fontSize = 11.sp, color = Muted.copy(alpha = 0.7f), modifier = Modifier.padding(start = 32.dp)) }
                    }
                }
            }
        }
        Box(modifier = Modifier.padding(20.dp)) {
            Button(onClick = { if (step == 1) step = 2 else onSubmit(Report(generateTicketId(), selectedType?.label ?: "Unknown", "Submitted", "10 May, 2026", address.ifEmpty { "Detected Location" }, severity, description, photoTaken, lat, lng)) }, enabled = selectedType != null && (step == 1 || photoTaken), modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text(if (step == 1) "NEXT STEP →" else "SUBMIT REPORT →", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun TrackerScreen(onBack: () -> Unit) {
    var ticketId by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Report?>(null) }
    var error by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBar(title = "Track Report", onBack = onBack)
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Enter your Ticket ID to check status", color = Muted, fontSize = 14.sp); Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = ticketId, onValueChange = { ticketId = it; error = false }, modifier = Modifier.weight(1f), placeholder = { Text("NR-XXXXX-XXX") }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White))
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = { val trimmed = ticketId.trim().uppercase(); val found = MOCK_REPORTS.find { it.ticketId == trimmed }; if (found != null) { result = found; error = false } else { result = null; error = true } }, modifier = Modifier.size(56.dp).background(Primary, RoundedCornerShape(12.dp))) { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) }
            }
            Spacer(modifier = Modifier.height(24.dp)); if (error) Text("Ticket not found. Please check the ID.", color = Red, fontWeight = FontWeight.Bold)
            result?.let { ReportItem(it) }
        }
    }
}

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopBar(title = "My Reports", onBack = onBack)
        LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(MOCK_REPORTS) { ReportItem(it) }
        }
    }
}

@Composable
fun ConfirmScreen(report: Report?, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Background), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).background(Green, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp)) }
        Spacer(modifier = Modifier.height(24.dp)); Text("Report Submitted!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Dark); Text("Ticket ID: ${report?.ticketId}", fontSize = 16.sp, color = Muted); Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onDone, modifier = Modifier.width(200.dp).height(50.dp), shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("DONE", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun ReportItem(report: Report) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardBackground), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.ticketId, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Surface(color = if (report.status == "Resolved") Green.copy(alpha = 0.1f) else Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp)) { Text(report.status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), color = if (report.status == "Resolved") Green else Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(8.dp)); Text(report.type, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Dark)
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Muted); Spacer(modifier = Modifier.width(4.dp)); Text(report.address, fontSize = 13.sp, color = Muted) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Border)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.date, color = Dark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Severity: ${report.severity}", color = if(report.severity == "Critical") Red else Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradBot, GradTop))).padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
        Spacer(modifier = Modifier.width(8.dp)); Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}
