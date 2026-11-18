package co.edu.unbosque.advancewars.enums;

/** Define los eventos que pueden registrarse en el historial de una partida. */
public enum GameLogType {
    GAME_CREATED,
    GAME_STARTED,
    TURN_STARTED,
    TURN_ENDED,
    UNIT_MOVED,
    UNIT_ATTACKED,
    UNIT_CAPTURE_STARTED,
    PROPERTY_CAPTURED,
    UNIT_PRODUCED,
    COMMANDER_POWER_USED,
    PLAYER_ELIMINATED,
    GAME_FINISHED
}
