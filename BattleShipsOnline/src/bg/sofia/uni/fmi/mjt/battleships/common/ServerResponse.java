package bg.sofia.uni.fmi.mjt.battleships.common;

public record ServerResponse(ResponseStatus status, String redirect, String message, String username) {

    public ServerResponse(ResponseStatus status, String redirect, String message) {
        this(status, redirect, message, null);
    }
}
