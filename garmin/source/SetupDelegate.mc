using Toybox.WatchUi as Ui;

class SetupDelegate extends Ui.BehaviorDelegate {
    hidden var match as MatchState;
    hidden var view as SetupView;

    function initialize(matchState as MatchState, setupView as SetupView) {
        BehaviorDelegate.initialize();
        match = matchState;
        view = setupView;
    }

    // UP button / swipe down
    function onPreviousPage() as Boolean {
        if (match.setupStep == Const.SETUP_PICK_SERVER) {
            view.pickedTeam = Const.TEAM_WE;
            Ui.requestUpdate();
        }
        return true;
    }

    // DOWN button / swipe up
    function onNextPage() as Boolean {
        if (match.setupStep == Const.SETUP_PICK_SERVER) {
            view.pickedTeam = Const.TEAM_THEY;
            Ui.requestUpdate();
        }
        return true;
    }

    // SELECT / ENTER / tap
    function onSelect() as Boolean {
        if (match.setupStep == Const.SETUP_START) {
            match.setupStep = Const.SETUP_PICK_SERVER;
            Ui.requestUpdate();
        } else {
            match.servingTeam = view.pickedTeam;
            match.isSetup = false;
            var scoreView = new ScoreView(match);
            var scoreDelegate = new ScoreDelegate(match, scoreView);
            Ui.switchToView(scoreView, scoreDelegate, Ui.SLIDE_LEFT);
        }
        return true;
    }

    function onBack() as Boolean {
        if (match.setupStep == Const.SETUP_PICK_SERVER) {
            match.setupStep = Const.SETUP_START;
            Ui.requestUpdate();
            return true;
        }
        return false; // let the system exit the app from the very first screen
    }
}
