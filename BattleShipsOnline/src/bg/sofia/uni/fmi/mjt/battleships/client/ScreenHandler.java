package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class ScreenHandler {

    private static final Function<ConsoleClient, ServerResponse> GUEST_HOME_HANDLER = (client) -> {
        try {
            return client.guestHomeScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Function<ConsoleClient, ServerResponse> REGISTER_HANDLER = (client) -> {
        try {
            return client.registerScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Function<ConsoleClient, ServerResponse> LOGIN_HANDLER = (client) -> {
        try {
            return client.loginScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Function<ConsoleClient, ServerResponse> HOME_HANDLER = (client) -> {
        try {
            return client.homeScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private static final Map<String, Function<ConsoleClient, ServerResponse>> SCREEN_HANDLERS = Map.of(
        ScreenInfo.GUEST_HOME_SCREEN, GUEST_HOME_HANDLER,
        ScreenInfo.REGISTER_SCREEN, REGISTER_HANDLER,
        ScreenInfo.LOGIN_SCREEN, LOGIN_HANDLER,
        ScreenInfo.HOME_SCREEN, HOME_HANDLER
    );

    private ConsoleClient client;

    private Function<ConsoleClient, ServerResponse> handler;

    public ScreenHandler(ConsoleClient client, String screen) {
        this.client = client;

        setHandler(screen);
    }

    public void setHandler(String screen) {
        this.handler = ScreenHandler.SCREEN_HANDLERS.get(screen);
    }

    public ServerResponse executeHandler() {
        var serverResponse = this.handler.apply(client);
        return serverResponse;
    }
}
