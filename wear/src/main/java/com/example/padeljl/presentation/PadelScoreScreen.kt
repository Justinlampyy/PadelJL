package com.example.padeljl.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.padeljl.BuildConfig
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import androidx.compose.ui.unit.sp

enum class Team { WE, THEY }
enum class Side { LEFT, RIGHT }
enum class SetupStep { START, PICK_SERVER }

data class SetScore(val we: Int = 0, val they: Int = 0)

data class MatchState(
    val wePoints: Int = 0,
    val theyPoints: Int = 0,
    val advantage: Team? = null,
    val deuceCount: Int = 0,
    val currentSet: Int = 0,
    val sets: List<SetScore> = listOf(SetScore(), SetScore(), SetScore()),
    val servingTeam: Team = Team.WE,
    val serviceSide: Side = Side.RIGHT,
    val isGoldenPoint: Boolean = false,
    val isMatchFinished: Boolean = false,
    val showSideSelection: Boolean = false,
    val isSetup: Boolean = true,
    val setupStep: SetupStep = SetupStep.START,
    val isTiebreak: Boolean = false,
    val tiebreakWe: Int = 0,
    val tiebreakThey: Int = 0
)

@Composable
fun PadelScoreScreen() {
    var state by remember { mutableStateOf(MatchState()) }
    val history = remember { mutableStateListOf<MatchState>() }
    var showStopDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dataClient = remember { Wearable.getDataClient(context) }

    val syncLiveScore: (MatchState) -> Unit = { newState ->
        val putDataMapReq = PutDataMapRequest.create("/live_score").apply {
            dataMap.putString("wePoints", formatScore(newState))
            dataMap.putString("theyPoints", formatScore(newState, isThey = true))
            dataMap.putInt("currentSet", newState.currentSet)
            dataMap.putString("servingTeam", newState.servingTeam.name)
            dataMap.putString("serviceSide", newState.serviceSide.name)
            val setsArray = newState.sets.map { "${it.we}-${it.they}" }.toTypedArray()
            dataMap.putStringArray("sets", setsArray)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent())
    }

    val sendFinalResult: (MatchState) -> Unit = { finalState ->
        val finalScore = finalState.sets.joinToString(" / ") { "${it.we}-${it.they}" }
        val putDataMapReq = PutDataMapRequest.create("/match_finished").apply {
            dataMap.putLong("date", System.currentTimeMillis())
            dataMap.putString("finalScore", finalScore)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        dataClient.putDataItem(putDataMapReq.asPutDataRequest().setUrgent())
    }

    val updateState: (MatchState) -> Unit = { newState ->
        history.add(state)
        state = newState
        syncLiveScore(newState)
        if (newState.isMatchFinished) {
            sendFinalResult(newState)
        }
    }

    val undo: () -> Unit = {
        if (history.isNotEmpty()) {
            state = history.removeAt(history.size - 1)
            syncLiveScore(state)
        }
    }

    val resetMatch: () -> Unit = {
        state = MatchState()
        history.clear()
        showStopDialog = false
        syncLiveScore(state)
    }

    if (state.isSetup) {
        SetupScreen(
            step = state.setupStep,
            onChoice = { choice ->
                when (choice) {
                    "PICK" -> state = state.copy(setupStep = SetupStep.PICK_SERVER)
                }
            },
            onResult = { team ->
                val newState = state.copy(servingTeam = team, isSetup = false)
                state = newState
                syncLiveScore(newState)
            }
        )
        return
    }

    val pointScored = { winner: Team ->
        if (!state.isMatchFinished) {
            var nextState = state.copy()
            
            if (nextState.isTiebreak) {
                if (winner == Team.WE) nextState = nextState.copy(tiebreakWe = nextState.tiebreakWe + 1)
                else nextState = nextState.copy(tiebreakThey = nextState.tiebreakThey + 1)
                
                if ((nextState.tiebreakWe >= 7 || nextState.tiebreakThey >= 7) && 
                    Math.abs(nextState.tiebreakWe - nextState.tiebreakThey) >= 2) {
                    nextState = awardSet(nextState, winner)
                } else {
                    // Side alternates every single point in a tiebreak (like normal deuce/ad alternation).
                    nextState = nextState.copy(serviceSide = if (nextState.serviceSide == Side.RIGHT) Side.LEFT else Side.RIGHT)
                    val totalPoints = nextState.tiebreakWe + nextState.tiebreakThey
                    if (totalPoints % 2 == 1) {
                       nextState = nextState.copy(servingTeam = if (nextState.servingTeam == Team.WE) Team.THEY else Team.WE)
                    }
                }
            } else if (nextState.isGoldenPoint) {
                nextState = awardGame(nextState, winner)
            } else if (nextState.advantage != null) {
                if (nextState.advantage == winner) {
                    nextState = awardGame(nextState, winner)
                } else {
                    val newDeuceCount = nextState.deuceCount + 1
                    nextState = nextState.copy(
                        advantage = null,
                        deuceCount = newDeuceCount,
                        isGoldenPoint = (newDeuceCount >= 3)
                    )
                    if (nextState.isGoldenPoint) nextState = nextState.copy(showSideSelection = true)
                }
            } else if (nextState.wePoints == 40 && nextState.theyPoints == 40) {
                nextState = nextState.copy(advantage = winner)
            } else {
                if (winner == Team.WE) {
                    when (nextState.wePoints) {
                        0 -> nextState = nextState.copy(wePoints = 15)
                        15 -> nextState = nextState.copy(wePoints = 30)
                        30 -> nextState = nextState.copy(wePoints = 40)
                        40 -> if (nextState.theyPoints < 40) nextState = awardGame(nextState, Team.WE) else nextState = nextState.copy(advantage = Team.WE)
                    }
                } else {
                    when (nextState.theyPoints) {
                        0 -> nextState = nextState.copy(theyPoints = 15)
                        15 -> nextState = nextState.copy(theyPoints = 30)
                        30 -> nextState = nextState.copy(theyPoints = 40)
                        40 -> if (nextState.wePoints < 40) nextState = awardGame(nextState, Team.THEY) else nextState = nextState.copy(advantage = Team.THEY)
                    }
                }
                if (nextState.wePoints == 40 && nextState.theyPoints == 40 && nextState.deuceCount == 0) nextState = nextState.copy(deuceCount = 1)
            }

            if (!nextState.isMatchFinished && !nextState.isTiebreak && (nextState.wePoints != 0 || nextState.theyPoints != 0 || nextState.advantage != null)) {
                if (!nextState.showSideSelection) nextState = nextState.copy(serviceSide = if (state.serviceSide == Side.RIGHT) Side.LEFT else Side.RIGHT)
            }
            updateState(nextState)
        }
    }

    SwipeToDismissBox(onDismissed = { }, modifier = Modifier.fillMaxSize().background(Color.Black)) { isBackground ->
        if (!isBackground) {
            Scaffold(timeText = { TimeText(modifier = Modifier.padding(top = 2.dp)) }) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ScoreCourtButton(Team.WE, "WIJ", formatScore(state), Color(0xFF2196F3), state.servingTeam == Team.WE, state.serviceSide, isTop = false, onClick = { pointScored(Team.WE) })
                        ScoreCourtButton(Team.THEY, "ZIJ", formatScore(state, isThey = true), Color(0xFFF44336), state.servingTeam == Team.THEY, state.serviceSide, isTop = true, onClick = { pointScored(Team.THEY) })
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val weSetsWon = state.sets.count { (it.we >= 6 && it.we - it.they >= 2) || it.we == 7 }
                    val theySetsWon = state.sets.count { (it.they >= 6 && it.they - it.we >= 2) || it.they == 7 }
                    Text("SETS: $weSetsWon - $theySetsWon", style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold), fontSize = 10.sp)
                    val currentSet = state.sets[state.currentSet]
                    Text("G: ${currentSet.we}-${currentSet.they}${if(state.isTiebreak) " (TB)" else ""}", style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold), fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        ActionButton(label = "UNDO", onClick = undo, width = 50.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        ActionButton(label = "STOP", onClick = { showStopDialog = true }, width = 50.dp)
                    }
                }
            }
            Dialog(showDialog = state.showSideSelection, onDismissRequest = { }) {
                Alert(title = { Text("Kies kant", textAlign = TextAlign.Center) },
                    negativeButton = { Button(onClick = { updateState(state.copy(serviceSide = Side.RIGHT, showSideSelection = false)) }) { Text("R") } },
                    positiveButton = { Button(onClick = { updateState(state.copy(serviceSide = Side.LEFT, showSideSelection = false)) }) { Text("L") } }
                ) { Text("Golden Point! Wie ontvangt?", textAlign = TextAlign.Center) }
            }
            Dialog(showDialog = showStopDialog, onDismissRequest = { showStopDialog = false }) {
                Alert(title = { Text("Match stoppen?", textAlign = TextAlign.Center) },
                    negativeButton = { Button(onClick = { sendFinalResult(state); resetMatch() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))) { Text("Opslaan", fontSize = 10.sp) } },
                    positiveButton = { Button(onClick = resetMatch, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))) { Text("Wissen", fontSize = 10.sp) } }
                ) { Text("Huidige stand opslaan of alles wissen?", textAlign = TextAlign.Center, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun SetupScreen(step: SetupStep, onChoice: (String) -> Unit, onResult: (Team) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            SetupStep.START -> {
                Text("Nieuwe Match", style = MaterialTheme.typography.title3)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onChoice("PICK") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Beginnen")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · ${BuildConfig.BUILD_TIME}",
                    style = MaterialTheme.typography.caption2,
                    fontSize = 8.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
            SetupStep.PICK_SERVER -> {
                Text("Wie serveert?", style = MaterialTheme.typography.title3)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onResult(Team.WE) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))) {
                        Text("Wij")
                    }
                    Button(onClick = { onResult(Team.THEY) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))) {
                        Text("Zij")
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCourtButton(team: Team, label: String, score: String, teamColor: Color, isServing: Boolean, side: Side, isTop: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.caption2, fontSize = 10.sp, color = teamColor)
        Button(
            onClick = onClick, 
            modifier = Modifier
                .size(width = 76.dp, height = 96.dp)
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)), 
            shape = RoundedCornerShape(8.dp), 
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0288D1))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val lineY = if (isTop) h * 0.4f else h * 0.6f
                    drawLine(Color.White, Offset(0f, lineY), Offset(w, lineY), strokeWidth = 1.dp.toPx(), alpha = 0.5f)
                    drawLine(Color.White, Offset(w / 2, if (isTop) lineY else 0f), Offset(w / 2, if (isTop) h else lineY), strokeWidth = 1.dp.toPx(), alpha = 0.5f)
                }

                Text(score, style = MaterialTheme.typography.title1.copy(fontWeight = FontWeight.Black), fontSize = if (score.length > 2) 20.sp else 28.sp, color = Color.White)
                
                if (isServing) {
                    val ballAlignment = if (team == Team.WE) {
                        if (side == Side.LEFT) Alignment.BottomStart else Alignment.BottomEnd
                    } else {
                        if (side == Side.LEFT) Alignment.TopEnd else Alignment.TopStart
                    }
                    Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = ballAlignment) {Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFCCFF00), CircleShape)
                                .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(label: String, onClick: () -> Unit, width: androidx.compose.ui.unit.Dp = 68.dp) {
    Button(onClick = onClick, modifier = Modifier.height(28.dp).width(width), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF455A64)), shape = RoundedCornerShape(14.dp)) {
        Text(label, style = MaterialTheme.typography.caption2.copy(fontWeight = FontWeight.Bold), fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

fun formatScore(state: MatchState, isThey: Boolean = false): String {
    if (state.isTiebreak) return if (isThey) state.tiebreakThey.toString() else state.tiebreakWe.toString()
    if (state.isGoldenPoint) return "GP"
    val adv = state.advantage
    if (adv != null) return if ((isThey && adv == Team.THEY) || (!isThey && adv == Team.WE)) "AD" else "--"
    return if (isThey) state.theyPoints.toString() else state.wePoints.toString()
}

fun awardGame(state: MatchState, winner: Team): MatchState {
    val newSets = state.sets.toMutableList()
    val currentSetScore = newSets[state.currentSet]
    var newWeGames = currentSetScore.we
    var newTheyGames = currentSetScore.they
    if (winner == Team.WE) newWeGames++ else newTheyGames++
    newSets[state.currentSet] = SetScore(newWeGames, newTheyGames)
    
    if (newWeGames == 6 && newTheyGames == 6) {
        return state.copy(
            wePoints = 0, theyPoints = 0, advantage = null, deuceCount = 0, isGoldenPoint = false,
            sets = newSets, isTiebreak = true, tiebreakWe = 0, tiebreakThey = 0,
            servingTeam = if (state.servingTeam == Team.WE) Team.THEY else Team.WE,
            serviceSide = Side.RIGHT
        )
    }

    val setIsOver = (newWeGames >= 6 && newWeGames - newTheyGames >= 2) || (newTheyGames >= 6 && newTheyGames - newWeGames >= 2) || (newWeGames == 7 || newTheyGames == 7)
    var nextSet = state.currentSet
    var matchFinished = false
    if (setIsOver) {
        if (nextSet == 2) matchFinished = true else nextSet++
    }
    return state.copy(wePoints = 0, theyPoints = 0, advantage = null, deuceCount = 0, isGoldenPoint = false, sets = newSets, currentSet = nextSet, servingTeam = if (state.servingTeam == Team.WE) Team.THEY else Team.WE, serviceSide = Side.RIGHT, isMatchFinished = matchFinished)
}

fun awardSet(state: MatchState, winner: Team): MatchState {
    val newSets = state.sets.toMutableList()
    val currentSetScore = newSets[state.currentSet]
    var newWeGames = currentSetScore.we
    var newTheyGames = currentSetScore.they
    if (winner == Team.WE) newWeGames = 7 else newTheyGames = 7
    newSets[state.currentSet] = SetScore(newWeGames, newTheyGames)
    
    var nextSet = state.currentSet
    var matchFinished = false
    if (nextSet == 2) matchFinished = true else nextSet++
    
    return state.copy(wePoints = 0, theyPoints = 0, advantage = null, deuceCount = 0, isGoldenPoint = false, isTiebreak = false, sets = newSets, currentSet = nextSet, servingTeam = if (state.servingTeam == Team.WE) Team.THEY else Team.WE, serviceSide = Side.RIGHT, isMatchFinished = matchFinished)
}
