package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;


import java.util.List;

public class Controller {

    protected ServerResponse messageResponse(String message, ClientState cookies, List<ServerResponse> signals) {
        return new ServerResponse(ResponseStatus.OK,
            message + getScreenPrompt(cookies.session.currentScreen, cookies),
            cookies, signals);
    }

    protected ServerResponse messageResponse(String message, ClientState cookies) {
        return messageResponse(message, cookies, null);
    }

    protected ServerResponse messageResponse(String message, ClientRequest request, List<ServerResponse> signals) {
        return messageResponse(message, request.cookies(), signals);
    }

    protected ServerResponse messageResponse(String message, ClientRequest request) {
        return messageResponse(message, request.cookies());
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

    protected ServerResponse redirectResponse(String screen, ClientRequest request, List<ServerResponse> signals) {
        return redirectResponse(screen, request, null, signals);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request, String message, List<ServerResponse> signals) {
        if (message == null) message = "";

        request.cookies().session.currentScreen = screen;
        return new ServerResponse(ResponseStatus.REDIRECT, message + getScreenPrompt(screen, request.cookies()), request.cookies(), signals);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request) {
        return redirectResponse(screen, request, null, null);
    }

    protected ServerResponse redirectResponse(String screen, ClientRequest request, String message) {
        return redirectResponse(screen, request, message, null);
    }

    protected ServerResponse invalidCommandResponse(ClientRequest request) {
        return this.invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), request);
    }

    protected ServerResponse invalidCommandResponse(String message, ClientRequest request) {
        return new ServerResponse(ResponseStatus.INVALID_COMMAND,
            message + getScreenPrompt(request.cookies().session.currentScreen, request.cookies()),
            request.cookies());
    }
}
