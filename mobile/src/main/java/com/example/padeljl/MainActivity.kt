package com.example.padeljl

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.wearable.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class Screen { SPLASH, MENU, LIVE, PLAY, HISTORY }

val PadelBrandGreen = Color(0xFFD4FA17)

@Suppress("DEPRECATION")
private fun android.view.Window.setStatusBarColorCompat(color: Int) {
    statusBarColor = color
}

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val matchHistory = mutableStateListOf<MatchResult>()
    private var liveState by mutableStateOf(LiveMatchState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        loadHistory()
        
        Wearable.getDataClient(this).addListener(this)
        
        setContent {
            val systemUiController = remember { WindowInsetsControllerCompat(window, window.decorView) }
            
            MaterialTheme(
                colors = darkColors(
                    primary = Color(0xFF2196F3),
                    primaryVariant = Color(0xFF1976D2),
                    secondary = Color(0xFF03DAC6),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onPrimary = Color.White,
                    onSecondary = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                SideEffect {
                    window.setStatusBarColorCompat(Color(0xFF121212).toArgb())
                    systemUiController.isAppearanceLightStatusBars = false
                }

                var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    delay(1500)
                    if (currentScreen == Screen.SPLASH) currentScreen = Screen.MENU
                }

                LaunchedEffect(currentScreen) {
                    val activity = context as? MainActivity
                    if (currentScreen == Screen.LIVE || currentScreen == Screen.PLAY) {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        val window = activity?.window
                        if (window != null) {
                            val controller = WindowCompat.getInsetsController(window, window.decorView)
                            controller.hide(WindowInsetsCompat.Type.systemBars())
                            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    } else {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        val window = activity?.window
                        if (window != null) {
                            val controller = WindowCompat.getInsetsController(window, window.decorView)
                            controller.show(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(
                        topBar = {
                            if (currentScreen == Screen.HISTORY) {
                                TopAppBar(
                                    title = { Text("Historie") },
                                    navigationIcon = {
                                        IconButton(onClick = { currentScreen = Screen.MENU }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                        }
                                    },
                                    backgroundColor = MaterialTheme.colors.surface,
                                    contentColor = MaterialTheme.colors.onSurface
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentScreen == Screen.LIVE) {
                                FloatingActionButton(
                                    onClick = { currentScreen = Screen.MENU },
                                    backgroundColor = Color.White.copy(alpha = 0.1f),
                                    contentColor = Color.White.copy(alpha = 0.3f),
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                    modifier = Modifier.padding(8.dp).size(40.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug naar Menu")
                                }
                            }
                        },
                        backgroundColor = Color.Transparent
                    ) { padding ->
                        Box(modifier = Modifier.padding(if (currentScreen == Screen.LIVE || currentScreen == Screen.PLAY || currentScreen == Screen.SPLASH) PaddingValues(0.dp) else padding)) {
                            when (currentScreen) {
                                Screen.SPLASH -> SplashScreen()
                                Screen.MENU -> MainMenuScreen(
                                    onNavigateToLive = { currentScreen = Screen.LIVE },
                                    onNavigateToPlay = { currentScreen = Screen.PLAY },
                                    onNavigateToHistory = { currentScreen = Screen.HISTORY }
                                )
                                Screen.LIVE -> LiveScoreLandscape(liveState)
                                Screen.PLAY -> PhoneScoreScreen(
                                    onMatchFinished = { finalScore ->
                                        val result = MatchResult(
                                            id = System.currentTimeMillis().toString(),
                                            date = System.currentTimeMillis(),
                                            finalScore = finalScore
                                        )
                                        matchHistory.add(0, result)
                                        saveHistory()
                                        currentScreen = Screen.MENU
                                    },
                                    onExit = { currentScreen = Screen.MENU }
                                )
                                Screen.HISTORY -> MatchHistoryScreen(
                                    history = matchHistory, 
                                    onUpdateMatch = { updatedMatch ->
                                        val index = matchHistory.indexOfFirst { it.id == updatedMatch.id }
                                        if (index != -1) {
                                            matchHistory[index] = updatedMatch
                                            saveHistory()
                                        }
                                    },
                                    onDeleteMatch = { matchToDelete ->
                                        matchHistory.remove(matchToDelete)
                                        saveHistory()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    private fun saveHistory() {
        val sharedPref = getSharedPreferences("padel_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(matchHistory.toList())
        sharedPref.edit { putString("match_history", json) }
    }

    private fun loadHistory() {
        val sharedPref = getSharedPreferences("padel_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("match_history", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<MatchResult>>() {}.type
                val list: List<MatchResult> = Gson().fromJson(json, type)
                matchHistory.clear()
                matchHistory.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                
                if (path == "/live_score") {
                    val setsArray = map.getStringArray("sets") ?: arrayOf("0-0", "0-0", "0-0")
                    val parsedSets = setsArray.map { 
                        val parts = it.split("-")
                        MatchSet(
                            we = parts.getOrNull(0)?.toIntOrNull() ?: 0,
                            they = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        )
                    }

                    liveState = LiveMatchState(
                        wePoints = map.getString("wePoints", "0"),
                        theyPoints = map.getString("theyPoints", "0"),
                        currentSet = map.getInt("currentSet"),
                        servingTeam = map.getString("servingTeam", "WE"),
                        serviceSide = map.getString("serviceSide", "RIGHT"),
                        sets = parsedSets
                    )
                } else if (path == "/match_finished") {
                    val timestamp = map.getLong("timestamp", 0L)
                    val date = map.getLong("date", System.currentTimeMillis())
                    val finalScore = map.getString("finalScore", "")
                    
                    if (timestamp != 0L) {
                        val id = timestamp.toString()
                        if (matchHistory.none { it.id == id }) {
                            val result = MatchResult(
                                id = id,
                                date = date,
                                finalScore = finalScore
                            )
                            matchHistory.add(0, result)
                            saveHistory()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.splash_mobile),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MenuActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF14171A).copy(alpha = 0.78f)),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.55f)),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(accentColor.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Text(
            label.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MainMenuScreen(onNavigateToLive: () -> Unit, onNavigateToPlay: () -> Unit, onNavigateToHistory: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_mobile),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.42f to Color.Transparent,
                        0.58f to Color.Black.copy(alpha = 0.75f),
                        0.72f to Color.Black.copy(alpha = 0.92f),
                        1.0f to Color.Black
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            MenuActionButton(
                label = "Live Score",
                icon = Icons.Default.LiveTv,
                accentColor = Color(0xFF4FC3F7),
                onClick = onNavigateToLive
            )

            Spacer(Modifier.height(18.dp))

            MenuActionButton(
                label = "Zelf Tellen",
                icon = Icons.Default.TouchApp,
                accentColor = PadelBrandGreen,
                onClick = onNavigateToPlay
            )

            Spacer(Modifier.height(18.dp))

            MenuActionButton(
                label = "Historie",
                icon = Icons.Default.History,
                accentColor = Color.White,
                onClick = onNavigateToHistory
            )

            Spacer(Modifier.height(56.dp))
        }

        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · build ${BuildConfig.BUILD_TIME}",
            fontSize = 11.sp,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
fun LiveScoreLandscape(state: LiveMatchState) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }

    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Text(
            text = currentTime,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCourtTile("WIJ", state.wePoints, Color(0xFF2196F3), state.servingTeam == "WE", state.serviceSide, isTopSide = false)
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                state.sets.forEachIndexed { index, set ->
                    val isCurrent = index == state.currentSet
                    Text(
                        "${set.we} - ${set.they}",
                        style = TextStyle(
                            fontSize = if (isCurrent) 100.sp else 50.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal,
                            color = if (isCurrent) Color.White else Color.Gray.copy(alpha = 0.4f),
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            ScoreCourtTile("ZIJ", state.theyPoints, Color(0xFFF44336), state.servingTeam == "THEY", state.serviceSide, isTopSide = true)
        }
    }
}

@Composable
fun ScoreCourtTile(label: String, points: String, teamColor: Color, isServingTeam: Boolean, serviceSide: String, isTopSide: Boolean, onClick: (() -> Unit)? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = teamColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 320.dp)
                .background(Color(0xFF0288D1), RoundedCornerShape(12.dp))
                .border(4.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            // Court Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val lineAlpha = 0.6f
                
                // Service line
                val serviceLineY = if (isTopSide) h * 0.35f else h * 0.65f
                drawLine(Color.White, Offset(0f, serviceLineY), Offset(w, serviceLineY), strokeWidth = 3.dp.toPx(), alpha = lineAlpha)
                
                // Center line (between service line and net/edge)
                val centerLineStart = if (isTopSide) serviceLineY else serviceLineY
                val centerLineEnd = if (isTopSide) h else 0f
                drawLine(Color.White, Offset(w / 2, centerLineStart), Offset(w / 2, centerLineEnd), strokeWidth = 3.dp.toPx(), alpha = lineAlpha)
            }

            Text(
                points,
                style = TextStyle(
                    fontSize = 220.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(8f, 8f),
                        blurRadius = 12f
                    )
                )
            )

            if (isServingTeam) {
                val isLeft = serviceSide == "LEFT"
                // Ball position logic based on side and team perspective
                val ballAlignment = if (isTopSide) {
                    if (isLeft) Alignment.TopEnd else Alignment.TopStart
                } else {
                    if (isLeft) Alignment.BottomStart else Alignment.BottomEnd
                }

                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp)
                        .align(ballAlignment),
                    shape = CircleShape,
                    color = Color(0xFFCCFF00),
                    elevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape))
                }
            }
        }
    }
}

@Composable
fun MatchHistoryScreen(
    history: List<MatchResult>, 
    onUpdateMatch: (MatchResult) -> Unit,
    onDeleteMatch: (MatchResult) -> Unit
) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nog geen wedstrijden gespeeld", color = Color.Gray, fontSize = 18.sp)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(history, key = { it.id }) { match ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = MaterialTheme.colors.onSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val locale = androidx.compose.ui.platform.LocalLocale.current.platformLocale
                            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", locale).format(Date(match.date))
                            Text(dateStr, color = Color.Gray, fontSize = 12.sp)
                            IconButton(onClick = { onDeleteMatch(match) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }

                        Text("Eindstand: ${match.finalScore}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(8.dp))
                        Text("Spelers Wij:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedTextField(
                                value = match.player1, 
                                onValueChange = { onUpdateMatch(match.copy(player1 = it)) }, 
                                label = { Text("Speler 1", fontSize = 10.sp) }, 
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = match.player2, 
                                onValueChange = { onUpdateMatch(match.copy(player2 = it)) }, 
                                label = { Text("Speler 2", fontSize = 10.sp) }, 
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text("Spelers Zij:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedTextField(
                                value = match.player3, 
                                onValueChange = { onUpdateMatch(match.copy(player3 = it)) }, 
                                label = { Text("Speler 1", fontSize = 10.sp) }, 
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = match.player4, 
                                onValueChange = { onUpdateMatch(match.copy(player4 = it)) }, 
                                label = { Text("Speler 2", fontSize = 10.sp) }, 
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = MaterialTheme.colors.onSurface,
                                    focusedBorderColor = MaterialTheme.colors.primary,
                                    unfocusedBorderColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
