package com.example.padeljl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhoneScoreScreen(onMatchFinished: (String) -> Unit, onExit: () -> Unit) {
    var state by remember { mutableStateOf(MatchState()) }
    val history = remember { mutableStateListOf<MatchState>() }
    var showStopDialog by remember { mutableStateOf(false) }

    fun finalScoreOf(s: MatchState) = s.sets.joinToString(" / ") { "${it.we}-${it.they}" }

    val updateState: (MatchState) -> Unit = { newState ->
        history.add(state)
        state = newState
        if (newState.isMatchFinished) {
            onMatchFinished(finalScoreOf(newState))
        }
    }

    val undo: () -> Unit = {
        if (history.isNotEmpty()) {
            state = history.removeAt(history.size - 1)
        }
    }

    if (state.isSetup) {
        PhoneSetupScreen(
            step = state.setupStep,
            onChoice = { state = state.copy(setupStep = SetupStep.PICK_SERVER) },
            onResult = { team -> state = state.copy(servingTeam = team, isSetup = false) },
            onCancel = onExit
        )
        return
    }

    val pointScored: (Team) -> Unit = { winner -> updateState(scorePoint(state, winner)) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        IconButton(
            onClick = { showStopDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Stoppen",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreCourtTile(
                "WIJ", formatScore(state), Color(0xFF2196F3),
                state.servingTeam == Team.WE, state.serviceSide.name, isTopSide = false,
                onClick = { pointScored(Team.WE) }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                state.sets.forEachIndexed { index, set ->
                    val isCurrent = index == state.currentSet
                    Text(
                        "${set.we} - ${set.they}",
                        fontSize = if (isCurrent) 72.sp else 36.sp,
                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal,
                        color = if (isCurrent) Color.White else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                if (state.isTiebreak) {
                    Text("TIEBREAK", color = Color(0xFFCCFF00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = undo,
                    enabled = history.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = if (history.isNotEmpty()) 0.85f else 0.25f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = if (history.isNotEmpty()) 0.4f else 0.15f)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("ONGEDAAN MAKEN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }

            ScoreCourtTile(
                "ZIJ", formatScore(state, isThey = true), Color(0xFFF44336),
                state.servingTeam == Team.THEY, state.serviceSide.name, isTopSide = true,
                onClick = { pointScored(Team.THEY) }
            )
        }
    }

    if (state.showSideSelection) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Golden Point!", fontWeight = FontWeight.Bold) },
            text = { Text("Wie ontvangt?") },
            confirmButton = {
                TextButton(onClick = { state = state.copy(serviceSide = Side.LEFT, showSideSelection = false) }) {
                    Text("Links", color = PadelBrandGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { state = state.copy(serviceSide = Side.RIGHT, showSideSelection = false) }) {
                    Text("Rechts", color = PadelBrandGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Match stoppen?", fontWeight = FontWeight.Bold) },
            text = { Text("Huidige stand opslaan of alles wissen?") },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    onMatchFinished(finalScoreOf(state))
                }) { Text("Opslaan", color = PadelBrandGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    onExit()
                }) { Text("Wissen", color = Color(0xFFF44336), fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun PhoneSetupScreen(step: SetupStep, onChoice: () -> Unit, onResult: (Team) -> Unit, onCancel: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                SetupStep.START -> {
                    Text(
                        "NIEUWE MATCH",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .background(PadelBrandGreen, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.height(40.dp))
                    Button(
                        onClick = onChoice,
                        modifier = Modifier.fillMaxWidth(0.65f).height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PadelBrandGreen),
                        elevation = ButtonDefaults.elevation(8.dp, 12.dp, 0.dp, 0.dp)
                    ) {
                        Text("BEGINNEN", fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.Black)
                    }
                    Spacer(Modifier.height(20.dp))
                    TextButton(onClick = onCancel) { Text("Annuleren", color = Color.Gray) }
                }
                SetupStep.PICK_SERVER -> {
                    Text(
                        "WIE SERVEERT?",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .background(PadelBrandGreen, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TeamChoiceButton("WIJ", Color(0xFF4FC3F7), onClick = { onResult(Team.WE) })
                        TeamChoiceButton("ZIJ", Color(0xFFF44336), onClick = { onResult(Team.THEY) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamChoiceButton(label: String, accentColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(150.dp).height(72.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF14171A).copy(alpha = 0.85f)),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = accentColor)
    }
}
