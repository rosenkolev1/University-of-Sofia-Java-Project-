package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;

public class UserController {

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
        if (password != null && !db.getUsers().contains(user)) {
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
            if (password.length() < 8) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_LENGTH);
                return serverResponse;
            }

            if (db.getUsers().stream().anyMatch(x -> x.username().equals(username))) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_USERNAME_TAKEN);
                return serverResponse;
            }

            if (db.getUsers().stream().anyMatch(x -> x.password().equals(password))) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_PASSWORD_TAKEN);
                return serverResponse;
            }
        }

        if (password != null) {

            db.addUser(username, password);

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

    private ServerResponse invalidCommandResponse() {
        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND));
    }

    private ServerResponse invalidCommandResponse(String message) {
        return new ServerResponse(ResponseStatus.INVALID_COMMAND, null, message);
    }
}
