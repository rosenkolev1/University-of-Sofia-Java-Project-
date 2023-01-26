package bg.sofia.uni.fmi.mjt.battleships.common;

public record ClientRequest(String input, SessionCookie session, GameCookie game) {
    public ClientRequest(String input, SessionCookie session) {
        this(input, session, null);
    }
}
