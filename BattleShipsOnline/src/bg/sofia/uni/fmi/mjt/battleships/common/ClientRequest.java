package bg.sofia.uni.fmi.mjt.battleships.common;

public record ClientRequest(String input, ClientState cookies) {
//    public ClientRequest(String input, SessionCookie session) {
//        this(input, session, null);
//    }
}
