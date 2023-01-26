package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;

import java.util.List;

public class HomeController extends Controller {
    private Database db;

    public HomeController(Database db) {
        this.db = db;
    }

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        if (command.command().equals(CommandInfo.CREATE_GAME)) {

            //Validate command
            if (args.length != 1) {
                serverResponse = invalidCommandResponse(request.session());
                return serverResponse;
            }

            var gameName = args[0];

            //Validate that the game name is valid
            if (gameName.isBlank()) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_NAME_NULL_EMPTY_BLANK, request.session());
                return serverResponse;
            }

            //Validate that the game has not been created already
            if (db.gameTable.gameExists(gameName)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_ALREADY_EXISTS, request.session());
                return serverResponse;
            }

            var curUser = db.userTable.getUser(request.session().username);

            var game = new Game(1, gameName, List.of(curUser), GameStatus.PENDING, true);

            db.gameTable.addGame(game);

            request.session().currentScreen = ScreenInfo.GAME_SCREEN;

            serverResponse = new ServerResponse(ResponseStatus.PENDING_GAME, ScreenInfo.GAME_SCREEN,
                ScreenUI.currentGame(gameName) + ScreenUI.GAME_PENDING_PROMPT, request.session());

        }
        else if (command.command().equals(CommandInfo.DELETE_GAME)) {
            serverResponse = new ServerResponse(ResponseStatus.JOINING_GAME, null,
                ScreenUI.PLACEHOLDER, request.session());
        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            request.session().username = null;
            request.session().currentScreen = ScreenInfo.GUEST_HOME_SCREEN;

            serverResponse = new ServerResponse(ResponseStatus.LOGOUT, ScreenInfo.GUEST_HOME_SCREEN,
                null, request.session());
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                    CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                    CommandInfo.LOG_OUT, CommandInfo.HELP), request.session());
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null,
                ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), request.session());
        }

        return serverResponse;
    }
}
