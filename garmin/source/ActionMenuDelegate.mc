using Toybox.WatchUi as Ui;

// Generic Menu2 delegate that forwards the chosen item's id back to
// whichever ScoreDelegate opened the menu (options / stop-confirm /
// golden-point side choice / restart-after-match menus all use this).
class ActionMenuDelegate extends Ui.Menu2InputDelegate {
    hidden var owner as ScoreDelegate;

    function initialize(ownerDelegate as ScoreDelegate) {
        Menu2InputDelegate.initialize();
        owner = ownerDelegate;
    }

    function onSelect(item as Ui.MenuItem) as Void {
        owner.handleMenuItem(item.getId() as String);
    }
}
