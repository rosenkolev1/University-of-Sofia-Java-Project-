package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;

public record ClientExceptionInfo(int port, String host, ClientState cookies) {
}
