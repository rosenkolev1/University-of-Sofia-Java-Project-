package bg.sofia.uni.fmi.mjt.battleships.server.controller.user;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.screen.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.annotation.Screen;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;

import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.List;

public class UserController extends Controller implements IUserController {

    private static final String VALID_REGISTRATION_REGEX = "^\\w{1,}$";

    public UserController(IDatabase db) {
        super(db);
    }

    @Screen(screen = ScreenInfo.LOGIN_SCREEN)
    @Override
    public ServerResponse loginRespond(ClientRequest request, List<SessionCookie> sessions) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        //Validate arguments
        if (args.length > 1) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var username = command.command();
        var password = args.length > 0 ? args[0] : null;

        var user = new User(username, password);

        //Validate that user exists
        if (password != null && !db.userTable().userExists(user)) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_USER_DOES_NOT_EXIST, request);
            return serverResponse;
        }

        //Validate that the user has not logged in already
        for (var session : sessions) {
            if (session != null && session.username != null && session.username.equals(username)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_USER_ALREADY_LOGGED_IN, request);
                return serverResponse;
            }
        }

        if (password != null) {
            request.cookies().session.username = username;
            serverResponse = redirectResponse(ScreenInfo.HOME_SCREEN, request, ScreenUI.SUCCESSFUL_LOGIN);
        }
        else if (request.input().equals(CommandInfo.BACK)) {
            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.LOGIN_CREDENTIALS, CommandInfo.BACK);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }

    @Screen(screen = ScreenInfo.REGISTER_SCREEN)
    @Override
    public ServerResponse registerRespond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        var username = command.command();
        var password = args.length > 0 ? args[0] : null;

        if (password != null) {
            //Validate arguments
            if (args.length > 1) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            //Check if password length is valid
            if (password.length() < 8) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_LENGTH, request);
                return serverResponse;
            }

            //Check if the username or password contains disallowed characters
            if (!username.matches(VALID_REGISTRATION_REGEX)) {
                return invalidCommandResponse(ScreenUI.INVALID_USERNAME_FORBIDDEN_CHARS, request);
            }
            else if (!password.matches(VALID_REGISTRATION_REGEX)) {
                return invalidCommandResponse(ScreenUI.INVALID_PASSWORD_FORBIDDEN_CHARS, request);
            }

            //Check if username is taken
            if (db.userTable().userExistWithName(username)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_USERNAME_TAKEN, request);
                return serverResponse;
            }

            //Check if password is taken
            if (db.userTable().userExistWithPassword(password)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_TAKEN, request);
                return serverResponse;
            }

            db.userTable().addUser(username, password);

            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request, ScreenUI.SUCCESSFUL_REGISTRATION);
        }
        else if (request.input().equals(CommandInfo.BACK)) {
            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.REGISTER_CREDENTIALS, CommandInfo.BACK, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }
}
