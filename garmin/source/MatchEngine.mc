import Toybox.Lang;

// Direct port of mobile/src/main/java/com/example/padeljl/PhoneMatchEngine.kt.
// Keep this file in sync with that one if the rules ever change there -
// the tiebreak serve/side rotation here was fixed and verified against
// official padel rules (see TiebreakRulesTest.kt in the Android project).
module MatchEngine {

    function formatScore(state as MatchState, isThey as Boolean) as String {
        if (state.isTiebreak) {
            return (isThey ? state.tiebreakThey : state.tiebreakWe).toString();
        }
        if (state.isGoldenPoint) {
            return "GP";
        }
        var adv = state.advantage;
        if (adv != null) {
            if ((isThey && adv == Const.TEAM_THEY) || (!isThey && adv == Const.TEAM_WE)) {
                return "AD";
            }
            return "--";
        }
        return (isThey ? state.theyPoints : state.wePoints).toString();
    }

    function awardGame(state as MatchState, winner as Number) as MatchState {
        var s = state.copy();
        var weGames = s.setsWe[s.currentSet];
        var theyGames = s.setsThey[s.currentSet];
        if (winner == Const.TEAM_WE) {
            weGames += 1;
        } else {
            theyGames += 1;
        }
        s.setsWe[s.currentSet] = weGames;
        s.setsThey[s.currentSet] = theyGames;

        if (weGames == 6 && theyGames == 6) {
            s.wePoints = 0;
            s.theyPoints = 0;
            s.advantage = null;
            s.deuceCount = 0;
            s.isGoldenPoint = false;
            s.isTiebreak = true;
            s.tiebreakWe = 0;
            s.tiebreakThey = 0;
            s.servingTeam = (state.servingTeam == Const.TEAM_WE) ? Const.TEAM_THEY : Const.TEAM_WE;
            s.serviceSide = Const.SIDE_RIGHT;
            return s;
        }

        var setIsOver = (weGames >= 6 && (weGames - theyGames) >= 2)
            || (theyGames >= 6 && (theyGames - weGames) >= 2)
            || (weGames == 7 || theyGames == 7);

        var nextSet = s.currentSet;
        var matchFinished = false;
        if (setIsOver) {
            if (nextSet == 2) {
                matchFinished = true;
            } else {
                nextSet += 1;
            }
        }

        s.wePoints = 0;
        s.theyPoints = 0;
        s.advantage = null;
        s.deuceCount = 0;
        s.isGoldenPoint = false;
        s.currentSet = nextSet;
        s.servingTeam = (state.servingTeam == Const.TEAM_WE) ? Const.TEAM_THEY : Const.TEAM_WE;
        s.serviceSide = Const.SIDE_RIGHT;
        s.isMatchFinished = matchFinished;
        return s;
    }

    function awardSet(state as MatchState, winner as Number) as MatchState {
        var s = state.copy();
        if (winner == Const.TEAM_WE) {
            s.setsWe[s.currentSet] = 7;
        } else {
            s.setsThey[s.currentSet] = 7;
        }

        var nextSet = s.currentSet;
        var matchFinished = false;
        if (nextSet == 2) {
            matchFinished = true;
        } else {
            nextSet += 1;
        }

        s.wePoints = 0;
        s.theyPoints = 0;
        s.advantage = null;
        s.deuceCount = 0;
        s.isGoldenPoint = false;
        s.isTiebreak = false;
        s.currentSet = nextSet;
        s.servingTeam = (state.servingTeam == Const.TEAM_WE) ? Const.TEAM_THEY : Const.TEAM_WE;
        s.serviceSide = Const.SIDE_RIGHT;
        s.isMatchFinished = matchFinished;
        return s;
    }

    function scorePoint(state as MatchState, winner as Number) as MatchState {
        if (state.isMatchFinished) {
            return state;
        }
        var next = state.copy();

        if (next.isTiebreak) {
            if (winner == Const.TEAM_WE) {
                next.tiebreakWe += 1;
            } else {
                next.tiebreakThey += 1;
            }

            var diff = next.tiebreakWe - next.tiebreakThey;
            if (diff < 0) {
                diff = -diff;
            }

            if ((next.tiebreakWe >= 7 || next.tiebreakThey >= 7) && diff >= 2) {
                next = awardSet(next, winner);
            } else {
                // Side alternates every single point in a tiebreak.
                next.serviceSide = (next.serviceSide == Const.SIDE_RIGHT) ? Const.SIDE_LEFT : Const.SIDE_RIGHT;
                var totalPoints = next.tiebreakWe + next.tiebreakThey;
                if (totalPoints % 2 == 1) {
                    next.servingTeam = (next.servingTeam == Const.TEAM_WE) ? Const.TEAM_THEY : Const.TEAM_WE;
                }
            }
        } else if (next.isGoldenPoint) {
            next = awardGame(next, winner);
        } else if (next.advantage != null) {
            if (next.advantage == winner) {
                next = awardGame(next, winner);
            } else {
                var newDeuceCount = next.deuceCount + 1;
                next.advantage = null;
                next.deuceCount = newDeuceCount;
                next.isGoldenPoint = (newDeuceCount >= 3);
                if (next.isGoldenPoint) {
                    next.showSideSelection = true;
                }
            }
        } else if (next.wePoints == 40 && next.theyPoints == 40) {
            next.advantage = winner;
        } else {
            if (winner == Const.TEAM_WE) {
                if (next.wePoints == 0) {
                    next.wePoints = 15;
                } else if (next.wePoints == 15) {
                    next.wePoints = 30;
                } else if (next.wePoints == 30) {
                    next.wePoints = 40;
                } else if (next.wePoints == 40) {
                    if (next.theyPoints < 40) {
                        next = awardGame(next, Const.TEAM_WE);
                    } else {
                        next.advantage = Const.TEAM_WE;
                    }
                }
            } else {
                if (next.theyPoints == 0) {
                    next.theyPoints = 15;
                } else if (next.theyPoints == 15) {
                    next.theyPoints = 30;
                } else if (next.theyPoints == 30) {
                    next.theyPoints = 40;
                } else if (next.theyPoints == 40) {
                    if (next.wePoints < 40) {
                        next = awardGame(next, Const.TEAM_THEY);
                    } else {
                        next.advantage = Const.TEAM_THEY;
                    }
                }
            }
            if (next.wePoints == 40 && next.theyPoints == 40 && next.deuceCount == 0) {
                next.deuceCount = 1;
            }
        }

        if (!next.isMatchFinished && !next.isTiebreak && (next.wePoints != 0 || next.theyPoints != 0 || next.advantage != null)) {
            if (!next.showSideSelection) {
                next.serviceSide = (state.serviceSide == Const.SIDE_RIGHT) ? Const.SIDE_LEFT : Const.SIDE_RIGHT;
            }
        }

        return next;
    }
}
