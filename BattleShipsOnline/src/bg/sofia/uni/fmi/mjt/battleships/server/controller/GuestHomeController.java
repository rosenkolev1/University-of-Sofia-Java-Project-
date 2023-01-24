package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;

public class GuestHomeController {

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        if (request.input().equals(CommandInfo.EXIT)) {
            serverResponse = new ServerResponse(ResponseStatus.EXIT, null,
                ScreenUI.EXIT_SUCCESS);
        }
        else if (request.input().equals(CommandInfo.LOGIN) || request.input().equals(CommandInfo.LOGIN_SHORTHAND)) {
            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.LOGIN_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.REGISTER) || request.input().equals(CommandInfo.REGISTER_SHORTHAND)) {
            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.REGISTER_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.REGISTER, CommandInfo.REGISTER_SHORTHAND,
                    CommandInfo.LOGIN, CommandInfo.LOGIN_SHORTHAND,
                    CommandInfo.EXIT, CommandInfo.HELP));
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null, ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND));
        }

        return serverResponse;
    }
}
