package bg.sofia.uni.fmi.mjt.battleships.common.request;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;

public record ClientRequest(String input, ClientState cookies) {
}
