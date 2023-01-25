package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;

import java.util.List;

public class GameController extends Controller {
    private Database db;

    public GameController(Database db) {
        this.db = db;
    }

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        if (request.input().equals(CommandInfo.LOG_OUT)) {
            serverResponse = new ServerResponse(ResponseStatus.REDIRECT, ScreenInfo.GUEST_HOME_SCREEN,
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
