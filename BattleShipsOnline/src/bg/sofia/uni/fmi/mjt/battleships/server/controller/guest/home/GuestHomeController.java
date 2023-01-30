package bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;


public class GuestHomeController extends Controller implements IGuestHomeController {

    public GuestHomeController(IDatabase db) {
        super(db);
    }

    @Override
    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        if (request.input() == null) {
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

    @Override
    public ServerResponse initialResponse(ClientRequest request, String channelNotEmptyString) {
        request.cookies().session = new SessionCookie(null, null);

        //Set the current screen manually
        request.cookies().session.currentScreen = ScreenInfo.GUEST_HOME_SCREEN;

        //The message of this response is the string which indicates to the client that the channel is not empty after reading chars from it
        //If the server buffer is not large enough to send all of the data at once
        return ServerResponse
            .builder()
            .setCookies(request.cookies())
            .setMessage(channelNotEmptyString)
            .build();
    }
}
