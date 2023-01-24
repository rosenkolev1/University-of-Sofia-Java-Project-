package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;

public class HomeController {

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        if (request.input().equals(request.username())) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.PLACEHOLDER);
        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            serverResponse = new ServerResponse(ResponseStatus.LOGOUT, ScreenInfo.GUEST_HOME_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.CREATE_GAME, CommandInfo.JOIN_GAME, CommandInfo.SAVED_GAMES,
                    CommandInfo.LOAD_GAME, CommandInfo.DELETE_GAME,
                    CommandInfo.LOG_OUT, CommandInfo.HELP));
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null, ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND));
        }

        return serverResponse;
    }
}
