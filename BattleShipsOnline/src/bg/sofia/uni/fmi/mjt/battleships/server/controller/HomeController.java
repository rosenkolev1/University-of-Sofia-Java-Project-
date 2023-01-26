package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            //Validate arguments
            if (args.length != 1) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            var gameName = args[0];

            //Validate that the game name is valid
            if (gameName.isBlank()) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_NAME_NULL_EMPTY_BLANK, request);
                return serverResponse;
            }

            //Validate that the game has not been created already
            if (db.gameTable.gameExists(gameName)) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_ALREADY_EXISTS, request);
                return serverResponse;
            }

            var curUser = db.userTable.getUser(request.session().username);

            var game = new Game(gameName, 2, GameStatus.PENDING, true, List.of(curUser));

            db.gameTable.addGame(game);

            request.session().currentScreen = ScreenInfo.GAME_SCREEN;

            serverResponse = new ServerResponse(ResponseStatus.PENDING_GAME, ScreenInfo.GAME_SCREEN,
                ScreenUI.currentGame(gameName) + ScreenUI.GAME_PENDING_PROMPT, request.session());

        }
        else if (command.command().equals(CommandInfo.JOIN_GAME)) {
            //Validate arguments
            if (args.length > 1) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            var gameName = args.length > 0 ? args[0] : null;

            //If there is no gameName, then choose a random game to join
            if (gameName == null) {
                var games = db.gameTable.pendingGames();

                //Validate that there are any games available
                if (games.size() == 0) {
                    serverResponse = invalidCommandResponse(ScreenUI.INVALID_NO_GAMES_AVAILABLE, request);
                    return serverResponse;
                }

                var random = new Random();
                var gameIndex = random.nextInt(0, games.size());

                var chosenGame = games.get(gameIndex);

               return joinGameResponse(request, chosenGame);
            }
            else {
                if (gameName.isBlank()) {
                    serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_NAME_NULL_EMPTY_BLANK, request);
                    return serverResponse;
                }
            }
        }
        else if (command.command().equals(CommandInfo.DELETE_GAME)) {
            serverResponse = new ServerResponse(ResponseStatus.JOINING_GAME, null,
                ScreenUI.PLACEHOLDER, request.session());
        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            request.session().username = null;
            request.session().currentScreen = ScreenInfo.GUEST_HOME_SCREEN;

            serverResponse = new ServerResponse(ResponseStatus.LOGOUT, ScreenInfo.GUEST_HOME_SCREEN,
                ScreenUI.SUCCESSFUL_LOGOUT, request.session());
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.LIST_GAMES,
                    CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                    CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                    CommandInfo.LOG_OUT, CommandInfo.HELP), request.session());
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null,
                ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), request.session());
        }

        return serverResponse;
    }

    private ServerResponse joinGameResponse(ClientRequest request, Game game) {
        var curUser = db.userTable.getUser(request.session().username);

        game.status = GameStatus.IN_PROGRESS;
        game.addPlayer(curUser);

        //Go to the game screen
        request.session().currentScreen = ScreenInfo.GAME_SCREEN;

        List<PlayerCookie> playersCookies = new ArrayList<>();

        for (var player : game.players) {
            var playerCookie = new PlayerCookie(player.user.username());

            playersCookies.add(playerCookie);
        }

        var curClientGameCookie = new GameCookie(game.name,1, 0, playersCookies);

        List<ServerResponse> signals = new ArrayList<>();

        var enemyPlayers = game.players.stream().filter(x -> !x.user.username().equals(curUser.username())).toList();

        for (var enemy : enemyPlayers) {
            var signalResponse = new ServerResponse(ResponseStatus.STARTING_GAME, null,
                ScreenUI.GAME_FOUND_OPPONENT + ScreenUI.GAME_STARTING,
                new SessionCookie(null, enemy.user.username()),
                new GameCookie(game.name,0, 0, playersCookies));

            signals.add(signalResponse);
        }

        var serverResponse = new ServerResponse(ResponseStatus.JOINING_GAME, null,
            ScreenUI.gameJoined(game.name), request.session(), curClientGameCookie, signals);

        return serverResponse;
    }
}
