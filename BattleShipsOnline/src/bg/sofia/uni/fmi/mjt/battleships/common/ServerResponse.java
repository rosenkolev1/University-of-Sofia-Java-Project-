package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.List;

public record ServerResponse(ResponseStatus status, String redirect, String message,
                             SessionCookie session, GameCookie game,
                             List<ServerResponse> signals) {

    public ServerResponse(ResponseStatus status, String redirect, String message,
                          SessionCookie session, GameCookie game) {
        this(status, redirect, message, session, game, null);
    }

    public ServerResponse(ResponseStatus status, String redirect, String message, SessionCookie session) {
        this(status, redirect, message, session, null, null);
    }
}
