import Toybox.Lang;
using Toybox.Application as App;
using Toybox.WatchUi as Ui;

class PadelApp extends App.AppBase {

    function initialize() {
        AppBase.initialize();
    }

    function onStart(state as Dictionary?) as Void {
    }

    function onStop(state as Dictionary?) as Void {
    }

    function getInitialView() as [Ui.Views] or [Ui.Views, Ui.InputDelegates] {
        var match = new MatchState();
        var view = new SetupView(match);
        var delegate = new SetupDelegate(match, view);
        return [view, delegate] as [Ui.Views, Ui.InputDelegates];
    }
}
