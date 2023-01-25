package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Game;

public class HomeController extends Controller {

    private Database db;

    public HomeController(Database db) {
        this.db = db;
    }

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        if (request.input().equals(request.username())) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.PLACEHOLDER);
        }
        else if (command.command().equals(CommandInfo.CREATE_GAME)) {

            //Validate command
            if (args.length != 1) {
                serverResponse = invalidCommandResponse();
                return serverResponse;
            }

            var gameName = args[0];

            if (gameName.isBlank()) {
                serverResponse = invalidCommandResponse("The name of the game is null, empty or blank!");
                return serverResponse;
            }



//            var user1 = db.getUser(request.username());
//            var game = new Game(user1, user1, true);
//
//            serverResponse = new ServerResponse(ResponseStatus.OK, null,
//                game.players.get(0).board.toString() + "\n" + game.players.get(1).board.toString());

        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            serverResponse = new ServerResponse(ResponseStatus.LOGOUT, ScreenInfo.GUEST_HOME_SCREEN,
                null);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                    CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                    CommandInfo.LOG_OUT, CommandInfo.HELP));
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null, ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND));
        }

        return serverResponse;
    }
}
