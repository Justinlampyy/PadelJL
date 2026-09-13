import Toybox.Lang;
using Toybox.WatchUi as Ui;
using Toybox.Graphics as Gfx;

class SetupView extends Ui.View {
    hidden var match as MatchState;
    var pickedTeam as Number; // Const.TEAM_WE or Const.TEAM_THEY, highlighted choice

    function initialize(matchState as MatchState) {
        View.initialize();
        match = matchState;
        pickedTeam = Const.TEAM_WE;
    }

    function onUpdate(dc as Gfx.Dc) as Void {
        dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();

        if (match.setupStep == Const.SETUP_START) {
            dc.drawText(w / 2, h * 0.32, Gfx.FONT_MEDIUM, "Nieuwe Match", Gfx.TEXT_JUSTIFY_CENTER);
            dc.setColor(Gfx.COLOR_DK_GRAY, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.55, Gfx.FONT_SMALL, "Druk op START", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);
        } else {
            dc.drawText(w / 2, h * 0.22, Gfx.FONT_MEDIUM, "Wie serveert?", Gfx.TEXT_JUSTIFY_CENTER);

            var weColor = (pickedTeam == Const.TEAM_WE) ? Gfx.COLOR_BLUE : Gfx.COLOR_DK_GRAY;
            var theyColor = (pickedTeam == Const.TEAM_THEY) ? Gfx.COLOR_RED : Gfx.COLOR_DK_GRAY;

            dc.setColor(weColor, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.45, Gfx.FONT_LARGE, "WIJ", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);

            dc.setColor(theyColor, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.65, Gfx.FONT_LARGE, "ZIJ", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);

            dc.setColor(Gfx.COLOR_DK_GRAY, Gfx.COLOR_TRANSPARENT);
            dc.drawText(w / 2, h * 0.85, Gfx.FONT_XTINY, "UP/DOWN kiezen, SELECT bevestigt", Gfx.TEXT_JUSTIFY_CENTER | Gfx.TEXT_JUSTIFY_VCENTER);
        }
    }
}
