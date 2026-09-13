import Toybox.Lang;

// Mirrors mobile/src/main/java/com/example/padeljl/PhoneMatchEngine.kt's MatchState
// so the exact same, already-tested rules apply here.
class MatchState {
    var wePoints as Number;
    var theyPoints as Number;
    var advantage as Number?; // null, Const.TEAM_WE or Const.TEAM_THEY
    var deuceCount as Number;
    var currentSet as Number;
    var setsWe as Array<Number>;   // games won per set index (3 sets)
    var setsThey as Array<Number>;
    var servingTeam as Number;     // Const.TEAM_WE / Const.TEAM_THEY
    var serviceSide as Number;     // Const.SIDE_LEFT / Const.SIDE_RIGHT
    var isGoldenPoint as Boolean;
    var isMatchFinished as Boolean;
    var showSideSelection as Boolean;
    var isSetup as Boolean;
    var setupStep as Number;       // Const.SETUP_START / Const.SETUP_PICK_SERVER
    var isTiebreak as Boolean;
    var tiebreakWe as Number;
    var tiebreakThey as Number;

    function initialize() {
        wePoints = 0;
        theyPoints = 0;
        advantage = null;
        deuceCount = 0;
        currentSet = 0;
        setsWe = [0, 0, 0];
        setsThey = [0, 0, 0];
        servingTeam = Const.TEAM_WE;
        serviceSide = Const.SIDE_RIGHT;
        isGoldenPoint = false;
        isMatchFinished = false;
        showSideSelection = false;
        isSetup = true;
        setupStep = Const.SETUP_START;
        isTiebreak = false;
        tiebreakWe = 0;
        tiebreakThey = 0;
    }

    function copy() as MatchState {
        var s = new MatchState();
        s.wePoints = wePoints;
        s.theyPoints = theyPoints;
        s.advantage = advantage;
        s.deuceCount = deuceCount;
        s.currentSet = currentSet;
        s.setsWe = copyArray(setsWe);
        s.setsThey = copyArray(setsThey);
        s.servingTeam = servingTeam;
        s.serviceSide = serviceSide;
        s.isGoldenPoint = isGoldenPoint;
        s.isMatchFinished = isMatchFinished;
        s.showSideSelection = showSideSelection;
        s.isSetup = isSetup;
        s.setupStep = setupStep;
        s.isTiebreak = isTiebreak;
        s.tiebreakWe = tiebreakWe;
        s.tiebreakThey = tiebreakThey;
        return s;
    }

    hidden function copyArray(src as Array<Number>) as Array<Number> {
        var out = new [src.size()];
        for (var i = 0; i < src.size(); i++) {
            out[i] = src[i];
        }
        return out;
    }
}
