package com.example.padeljl

enum class Team { WE, THEY }
enum class Side { LEFT, RIGHT }
enum class SetupStep { START, PICK_SERVER }

data class MatchState(
    val wePoints: Int = 0,
    val theyPoints: Int = 0,
    val advantage: Team? = null,
    val deuceCount: Int = 0,
    val currentSet: Int = 0,
    val sets: List<MatchSet> = listOf(MatchSet(0, 0), MatchSet(0, 0), MatchSet(0, 0)),
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
    newSets[state.currentSet] = MatchSet(newWeGames, newTheyGames)

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
    newSets[state.currentSet] = MatchSet(newWeGames, newTheyGames)

    var nextSet = state.currentSet
    var matchFinished = false
    if (nextSet == 2) matchFinished = true else nextSet++

    return state.copy(wePoints = 0, theyPoints = 0, advantage = null, deuceCount = 0, isGoldenPoint = false, isTiebreak = false, sets = newSets, currentSet = nextSet, servingTeam = if (state.servingTeam == Team.WE) Team.THEY else Team.WE, serviceSide = Side.RIGHT, isMatchFinished = matchFinished)
}

fun scorePoint(state: MatchState, winner: Team): MatchState {
    if (state.isMatchFinished) return state
    var nextState = state.copy()

    if (nextState.isTiebreak) {
        nextState = if (winner == Team.WE) nextState.copy(tiebreakWe = nextState.tiebreakWe + 1)
        else nextState.copy(tiebreakThey = nextState.tiebreakThey + 1)

        if ((nextState.tiebreakWe >= 7 || nextState.tiebreakThey >= 7) &&
            kotlin.math.abs(nextState.tiebreakWe - nextState.tiebreakThey) >= 2) {
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
                40 -> nextState = if (nextState.theyPoints < 40) awardGame(nextState, Team.WE) else nextState.copy(advantage = Team.WE)
            }
        } else {
            when (nextState.theyPoints) {
                0 -> nextState = nextState.copy(theyPoints = 15)
                15 -> nextState = nextState.copy(theyPoints = 30)
                30 -> nextState = nextState.copy(theyPoints = 40)
                40 -> nextState = if (nextState.wePoints < 40) awardGame(nextState, Team.THEY) else nextState.copy(advantage = Team.THEY)
            }
        }
        if (nextState.wePoints == 40 && nextState.theyPoints == 40 && nextState.deuceCount == 0) nextState = nextState.copy(deuceCount = 1)
    }

    if (!nextState.isMatchFinished && !nextState.isTiebreak && (nextState.wePoints != 0 || nextState.theyPoints != 0 || nextState.advantage != null)) {
        if (!nextState.showSideSelection) nextState = nextState.copy(serviceSide = if (state.serviceSide == Side.RIGHT) Side.LEFT else Side.RIGHT)
    }
    return nextState
}
