package bg.sofia.uni.fmi.mjt.battleships.common;

public enum ResponseStatus {
    OK,
    REDIRECT,
    INVALID_COMMAND,
    LOGIN,
    LOGOUT,
    PENDING_GAME,
    START_GAME,
    JOIN_GAME,
    QUIT_GAME,
    RESUME_GAME,
    FINISH_GAME,
    UNEXPECTED_ERROR,
    EXIT
}
