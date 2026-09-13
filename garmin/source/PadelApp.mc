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

    function getInitialView() as Array<Ui.Views or Ui.InputDelegates>? {
        var match = new MatchState();
        var view = new SetupView(match);
        var delegate = new SetupDelegate(match, view);
        return [view, delegate] as Array<Ui.Views or Ui.InputDelegates>;
    }
}
