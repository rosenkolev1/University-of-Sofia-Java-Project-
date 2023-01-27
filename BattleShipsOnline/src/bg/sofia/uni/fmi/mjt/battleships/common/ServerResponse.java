package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.List;

public record ServerResponse(ResponseStatus status, String message, ClientState cookies, List<ServerResponse> signals) {

    public ServerResponse(ResponseStatus status, String message, ClientState cookies) {
        this(status, message, cookies, null);
    }
//
//    public ServerResponse(ResponseStatus status, String redirect, String message, SessionCookie session) {
//        this(status, redirect, message, session, null, null);
//    }
}
