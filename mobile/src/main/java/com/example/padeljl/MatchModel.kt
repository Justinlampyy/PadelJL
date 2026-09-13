package com.example.padeljl

data class MatchSet(val we: Int, val they: Int)

data class MatchResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val teamWe: String = "Wij",
    val teamThey: String = "Zij",
    val player1: String = "",
    val player2: String = "",
    val player3: String = "",
    val player4: String = "",
    val sets: List<MatchSet> = listOf(),
    val finalScore: String = ""
)

data class LiveMatchState(
    val wePoints: String = "0",
    val theyPoints: String = "0",
    val sets: List<MatchSet> = listOf(MatchSet(0, 0), MatchSet(0, 0), MatchSet(0, 0)),
    val currentSet: Int = 0,
    val servingTeam: String = "WE",
    val serviceSide: String = "RIGHT",
    val isFinished: Boolean = false
)
