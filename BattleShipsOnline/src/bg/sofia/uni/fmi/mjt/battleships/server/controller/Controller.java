package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;


import java.util.List;

public class Controller {

    protected ServerResponse messageResponse(ServerResponse response) {
        response.message += getScreenPrompt(response.cookies.session.currentScreen, response.cookies);
        return response;
    }

    protected ServerResponse messageResponse(ServerResponse.Builder builder) {
        var response = builder.build();
        return messageResponse(response);
    }

    protected String getScreenPrompt(String screen, ClientState cookies) {
        var screenPrompt = ScreenUI.SCREENS_PROMPTS.get(screen).apply(cookies);
        return "-".repeat(100) + (screenPrompt == null ? "" : screenPrompt);
    }

    protected ServerResponse helpResponse(ClientRequest request, String... commands) {
        return new ServerResponse(ResponseStatus.OK,
            ScreenUI.getAvailableCommands(commands) + getScreenPrompt(request.cookies().session.currentScreen, request.cookies()),
            request.cookies());
    }

    protected ServerResponse redirectResponse(String screen, ServerResponse response) {
        if (response.message == null) response.message = "";

        response.cookies.session.currentScreen = screen;
        response.message += getScreenPrompt(screen, response.cookies);

        return response;
    }

    protected ServerResponse redirectResponse(String screen, ServerResponse.Builder builder) {
        var response = builder.build();
        return redirectResponse(screen, response);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request, String message, List<ServerResponse> signals) {
        return redirectResponse(screen,
            ServerResponse
                .builder()
                .setCookies(request.cookies())
                .setMessage(message)
                .setSignals(signals)
        );
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request, List<ServerResponse> signals) {
        return redirectResponse(screen, request, null, signals);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request, String message) {
        return redirectResponse(screen, request, message, null);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request) {
        return redirectResponse(screen, request, null, null);
    }

    protected ServerResponse invalidCommandResponse(ServerResponse.Builder builder) {
        var response = builder.build();
        return invalidCommandResponse(response);
    }

    protected ServerResponse invalidCommandResponse(ServerResponse response) {
        response.message += getScreenPrompt(response.cookies.session.currentScreen, response.cookies);
        response.status = ResponseStatus.INVALID_COMMAND;
        return response;
    }

    protected ServerResponse invalidCommandResponse(ClientRequest request) {
        return invalidCommandResponse(request.cookies());
    }

    protected ServerResponse invalidCommandResponse(ClientState cookies) {
        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), cookies);
    }

    protected ServerResponse invalidCommandResponse(String message, ClientRequest request) {
        return invalidCommandResponse(message, request.cookies());
    }

    protected ServerResponse invalidCommandResponse(String message, ClientState cookies) {
        return invalidCommandResponse(
            ServerResponse
                .builder()
                .setMessage(message)
                .setCookies(cookies)
        );
    }
}
