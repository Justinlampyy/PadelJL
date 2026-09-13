package com.example.padeljl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TiebreakRulesTest {

    @Test
    fun `entering a tiebreak hands serve to the alternating team and resets side to right`() {
        var state = MatchState()
        // Play 12 games (6-6) by alternating winners; awardGame flips serve after every normal game.
        val winners = List(12) { if (it % 2 == 0) Team.WE else Team.THEY }
        for (w in winners) {
            state = awardGame(state, w)
        }

        assertTrue("should be in a tiebreak at 6-6", state.isTiebreak)
        assertEquals(Side.RIGHT, state.serviceSide)
        // 12 games alternated from an initial server of WE -> the correctly alternated
        // next server (as if there were a 13th game) is WE again.
        assertEquals(Team.WE, state.servingTeam)
    }

    @Test
    fun `tiebreak serve rotates 1-2-2-2 and side alternates every point`() {
        var state = MatchState()
        val winners = List(12) { if (it % 2 == 0) Team.WE else Team.THEY }
        for (w in winners) state = awardGame(state, w)

        val serverBeforeEachPoint = mutableListOf<Team>()
        val sideBeforeEachPoint = mutableListOf<Side>()

        repeat(6) {
            serverBeforeEachPoint.add(state.servingTeam)
            sideBeforeEachPoint.add(state.serviceSide)
            state = scorePoint(state, Team.WE) // winner doesn't affect serve/side rotation
        }

        // Official rule: first server serves 1 point, then serve alternates every 2 points: A,B,B,A,A,B
        assertEquals(listOf(Team.WE, Team.THEY, Team.THEY, Team.WE, Team.WE, Team.THEY), serverBeforeEachPoint)

        // Side must alternate every single point: R,L,R,L,R,L
        assertEquals(
            listOf(Side.RIGHT, Side.LEFT, Side.RIGHT, Side.LEFT, Side.RIGHT, Side.LEFT),
            sideBeforeEachPoint
        )
    }
}
