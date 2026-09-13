import Toybox.Lang;
using Toybox.WatchUi as Ui;
using Toybox.Graphics as Gfx;

class ScoreView extends Ui.View {
    hidden var match as MatchState;

    function initialize(matchState as MatchState) {
        View.initialize();
        match = matchState;
    }

    function onUpdate(dc as Gfx.Dc) as Void {
        dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();

        if (match.isMatchFinished) {
            var weSets = countSetsWon(Const.TEAM_WE);
            var theySets = countSetsWon(Const.TEAM_THEY);
            var winnerText = (weSets > theySets) ? "WIJ winnen!" : "ZIJ winnen!";
            dc.drawText(w / 2, h * 0.35, Gfx.FONT_MEDIUM, winnerText, Gfx.TEXT_JUSTIFY_CENTER);
            dc.drawText(w / 2, h * 0.55, Gfx.FONT_SMALL, "Sets: " + weSets + " - " + theySets, Gfx.TEXT_JUSTIFY_CENTER);
            dc.setColor(Gfx.COLOR_DK_GRAY, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.8, Gfx.FONT_XTINY, "MENU voor nieuwe match", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);
            return;
        }

        // Team labels
        dc.setColor(Gfx.COLOR_BLUE, Gfx.COLOR_TRANSPARENT);
        dc.drawText(w * 0.25, h * 0.08, Gfx.FONT_TINY, "WIJ", Gfx.TEXT_JUSTIFY_CENTER);
        dc.setColor(Gfx.COLOR_RED, Gfx.COLOR_TRANSPARENT);
        dc.drawText(w * 0.75, h * 0.08, Gfx.FONT_TINY, "ZIJ", Gfx.TEXT_JUSTIFY_CENTER);

        // Points
        dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_TRANSPARENT);
        dc.drawText(w * 0.25, h * 0.28, Gfx.FONT_NUMBER_MEDIUM, MatchEngine.formatScore(match, false), Gfx.TEXT_JUSTIFY_CENTER);
        dc.drawText(w * 0.75, h * 0.28, Gfx.FONT_NUMBER_MEDIUM, MatchEngine.formatScore(match, true), Gfx.TEXT_JUSTIFY_CENTER);

        // Serve indicator: small dot under the serving team, offset left/right by side
        var servingX = (match.servingTeam == Const.TEAM_WE) ? w * 0.25 : w * 0.75;
        var dotOffset = (match.serviceSide == Const.SIDE_LEFT) ? -w * 0.12 : w * 0.12;
        dc.setColor(Gfx.COLOR_YELLOW, Gfx.COLOR_TRANSPARENT);
        dc.fillCircle(servingX + dotOffset, h * 0.4, 5);

        // Sets / games line
        dc.setColor(Gfx.COLOR_LT_GRAY, Gfx.COLOR_TRANSPARENT);
        var setsLine = "";
        for (var i = 0; i < match.setsWe.size(); i++) {
            setsLine += match.setsWe[i] + "-" + match.setsThey[i];
            if (i < match.setsWe.size() - 1) {
                setsLine += "  ";
            }
        }
        dc.drawText(w / 2, h * 0.58, Gfx.FONT_XTINY, setsLine, Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);

        if (match.isTiebreak) {
            dc.setColor(Gfx.COLOR_YELLOW, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.68, Gfx.FONT_XTINY, "TIEBREAK", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);
        }

        // Controls hint
        dc.setColor(Gfx.COLOR_DK_GRAY, Gfx.COLOR_TRANSPARENT);
        dc.drawText(w / 2, h * 0.9, Gfx.FONT_XTINY, "UP: WIJ  DOWN: ZIJ  MENU: opties", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);
    }

    hidden function countSetsWon(team as Number) as Number {
        var count = 0;
        for (var i = 0; i < match.setsWe.size(); i++) {
            var we = match.setsWe[i];
            var they = match.setsThey[i];
            var weWon = (we >= 6 && (we - they) >= 2) || (we == 7);
            var theyWon = (they >= 6 && (they - we) >= 2) || (they == 7);
            if (team == Const.TEAM_WE && weWon) {
                count += 1;
            } else if (team == Const.TEAM_THEY && theyWon) {
                count += 1;
            }
        }
        return count;
    }
}
