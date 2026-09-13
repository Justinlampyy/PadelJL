using Toybox.WatchUi as Ui;

class ScoreDelegate extends Ui.BehaviorDelegate {
    hidden var match as MatchState;
    hidden var view as ScoreView;
    hidden var history as Array<MatchState>;

    function initialize(matchState as MatchState, scoreView as ScoreView) {
        BehaviorDelegate.initialize();
        match = matchState;
        view = scoreView;
        history = [];
    }

    // UP button / swipe down -> WIJ scores
    function onPreviousPage() as Boolean {
        scorePoint(Const.TEAM_WE);
        return true;
    }

    // DOWN button / swipe up -> ZIJ scores
    function onNextPage() as Boolean {
        scorePoint(Const.TEAM_THEY);
        return true;
    }

    hidden function scorePoint(winner as Number) as Void {
        if (match.isMatchFinished) {
            return;
        }
        pushHistory(match.copy());
        var updated = MatchEngine.scorePoint(match, winner);
        copyInto(match, updated);

        if (match.showSideSelection) {
            showSideMenu();
        }
        Ui.requestUpdate();
    }

    // MENU button / long-press / touch-and-hold
    function onMenu() as Boolean {
        if (match.isMatchFinished) {
            showFinishedMenu();
        } else {
            showOptionsMenu();
        }
        return true;
    }

    // BACK button / swipe right -> ask to stop, instead of instantly exiting
    function onBack() as Boolean {
        if (match.isMatchFinished) {
            return false;
        }
        showStopConfirm();
        return true;
    }

    hidden function showOptionsMenu() as Void {
        var menu = new Ui.Menu2({:title => "Opties"});
        if (history.size() > 0) {
            menu.addItem(new Ui.MenuItem("Ongedaan maken", null, "undo", {}));
        }
        menu.addItem(new Ui.MenuItem("Stoppen", null, "stop", {}));
        Ui.pushView(menu, new ActionMenuDelegate(self), Ui.SLIDE_UP);
    }

    hidden function showStopConfirm() as Void {
        var menu = new Ui.Menu2({:title => "Match stoppen?"});
        menu.addItem(new Ui.MenuItem("Ja, stoppen", null, "stop_yes", {}));
        menu.addItem(new Ui.MenuItem("Nee, doorgaan", null, "stop_no", {}));
        Ui.pushView(menu, new ActionMenuDelegate(self), Ui.SLIDE_UP);
    }

    hidden function showSideMenu() as Void {
        var menu = new Ui.Menu2({:title => "Golden Point! Wie ontvangt?"});
        menu.addItem(new Ui.MenuItem("Links", null, "side_left", {}));
        menu.addItem(new Ui.MenuItem("Rechts", null, "side_right", {}));
        Ui.pushView(menu, new ActionMenuDelegate(self), Ui.SLIDE_UP);
    }

    hidden function showFinishedMenu() as Void {
        var menu = new Ui.Menu2({:title => "Match klaar"});
        menu.addItem(new Ui.MenuItem("Nieuwe match", null, "restart", {}));
        Ui.pushView(menu, new ActionMenuDelegate(self), Ui.SLIDE_UP);
    }

    // Called back by ActionMenuDelegate when a menu item is chosen.
    function handleMenuItem(id as String) as Void {
        if (id.equals("undo")) {
            var previous = popHistory();
            if (previous != null) {
                copyInto(match, previous);
            }
        } else if (id.equals("stop") || id.equals("stop_yes")) {
            restartMatch();
        } else if (id.equals("stop_no")) {
            // no-op, just close the menu
        } else if (id.equals("side_left")) {
            match.serviceSide = Const.SIDE_LEFT;
            match.showSideSelection = false;
        } else if (id.equals("side_right")) {
            match.serviceSide = Const.SIDE_RIGHT;
            match.showSideSelection = false;
        } else if (id.equals("restart")) {
            restartMatch();
        }
        Ui.requestUpdate();
    }

    hidden function restartMatch() as Void {
        var fresh = new MatchState();
        var setupView = new SetupView(fresh);
        var setupDelegate = new SetupDelegate(fresh, setupView);
        Ui.switchToView(setupView, setupDelegate, Ui.SLIDE_RIGHT);
    }

    hidden function pushHistory(s as MatchState) as Void {
        var n = history.size();
        var newHist = new [n + 1];
        for (var i = 0; i < n; i++) {
            newHist[i] = history[i];
        }
        newHist[n] = s;
        history = newHist;
    }

    hidden function popHistory() as MatchState? {
        var n = history.size();
        if (n == 0) {
            return null;
        }
        var last = history[n - 1];
        var newHist = new [n - 1];
        for (var i = 0; i < n - 1; i++) {
            newHist[i] = history[i];
        }
        history = newHist;
        return last;
    }

    // Copies every field of src into dst in place, since the view/delegate
    // hold a reference to one shared MatchState instance.
    hidden function copyInto(dst as MatchState, src as MatchState) as Void {
        dst.wePoints = src.wePoints;
        dst.theyPoints = src.theyPoints;
        dst.advantage = src.advantage;
        dst.deuceCount = src.deuceCount;
        dst.currentSet = src.currentSet;
        dst.setsWe = src.setsWe;
        dst.setsThey = src.setsThey;
        dst.servingTeam = src.servingTeam;
        dst.serviceSide = src.serviceSide;
        dst.isGoldenPoint = src.isGoldenPoint;
        dst.isMatchFinished = src.isMatchFinished;
        dst.showSideSelection = src.showSideSelection;
        dst.isSetup = src.isSetup;
        dst.setupStep = src.setupStep;
        dst.isTiebreak = src.isTiebreak;
        dst.tiebreakWe = src.tiebreakWe;
        dst.tiebreakThey = src.tiebreakThey;
    }
}
