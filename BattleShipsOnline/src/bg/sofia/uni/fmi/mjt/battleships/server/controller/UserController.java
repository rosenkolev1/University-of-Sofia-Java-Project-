package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;

public class UserController extends Controller {

    private static final String VALID_REGISTRATION_REGEX = "^\\w{1,}$";

    private Database db;

    public UserController(Database db) {
        this.db = db;
    }

    public ServerResponse loginResponse(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        //Validate command
        if (args.length > 1) {
            serverResponse = invalidCommandResponse();
            return serverResponse;
        }

        var username = command.command();
        var password = args.length > 0 ? args[0] : null;

        var user = new User(username, password);

        //Validate that user exists
        if (password != null && !db.userTable.getUsers().contains(user)) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_USER_DOES_NOT_EXIST);
            return serverResponse;
        }

        if (password != null) {
            serverResponse = new ServerResponse(ResponseStatus.LOGIN, ScreenInfo.HOME_SCREEN,
                ScreenUI.SUCCESSFUL_LOGIN, username);
        }
        else if (request.input().equals(CommandInfo.BACK)) {
            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.GUEST_HOME_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.LOGIN_CREDENTIALS, CommandInfo.BACK));
        }
        else {
            serverResponse = invalidCommandResponse();
        }

        return serverResponse;
    }

    public ServerResponse registerResponse(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        //Validate command
        if (args.length > 1) {
            serverResponse = invalidCommandResponse();
            return serverResponse;
        }

        var username = command.command();
        var password = args.length > 0 ? args[0] : null;

        //Validate password
        if (password != null) {

            //Check if password length is valid
            if (password.length() < 8) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_LENGTH);
                return serverResponse;
            }

            //Check if the username or password contains unallowed characters
            if (!username.matches(VALID_REGISTRATION_REGEX)) {

                return invalidCommandResponse(ScreenUI.INVALID_USERNAME_FORBIDDEN_CHARS);
            }
            else if (!password.matches(VALID_REGISTRATION_REGEX)) {

                return invalidCommandResponse(ScreenUI.INVALID_PASSWORD_FORBIDDEN_CHARS);
            }

            //Check if username is taken
            if (db.userTable.userExistWithName(username)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_USERNAME_TAKEN);
                return serverResponse;
            }

            //Check if password is taken
            if (db.userTable.userExistWithPassword(password)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_TAKEN);
                return serverResponse;
            }
        }

        if (password != null) {
            db.userTable.addUser(username, password);

            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.GUEST_HOME_SCREEN,
                ScreenUI.SUCCESSFUL_REGISTRATION);
        }
        else if (request.input().equals(CommandInfo.BACK)) {
            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.GUEST_HOME_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(CommandInfo.REGISTER_CREDENTIALS, CommandInfo.BACK, CommandInfo.HELP));
        }
        else {
            serverResponse = invalidCommandResponse();
        }

        return serverResponse;
    }

}
