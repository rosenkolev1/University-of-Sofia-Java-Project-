package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;


public class GuestHomeController extends Controller {

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        if (request.cookies().session == null) {
            request.cookies().session = new SessionCookie(null, null);
            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request);
        }
        else if (request.input().equals(CommandInfo.EXIT)) {
            serverResponse = new ServerResponse(ResponseStatus.EXIT, ScreenUI.EXIT_SUCCESS, request.cookies());
        }
        else if (request.input().equals(CommandInfo.LOGIN) || request.input().equals(CommandInfo.LOGIN_SHORTHAND)) {
            serverResponse = redirectResponse(ScreenInfo.LOGIN_SCREEN, request);
        }
        else if (request.input().equals(CommandInfo.REGISTER) || request.input().equals(CommandInfo.REGISTER_SHORTHAND)) {
            serverResponse = redirectResponse(ScreenInfo.REGISTER_SCREEN, request);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.REGISTER, CommandInfo.REGISTER_SHORTHAND,
                CommandInfo.LOGIN, CommandInfo.LOGIN_SHORTHAND,
                CommandInfo.EXIT, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }
}
