package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;

import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

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

            //Validate that an unfinished game with the same name has not been created already
            if (db.gameTable.games.stream()
                .anyMatch(x -> x.name.equals(gameName) && x.status != GameStatus.ENDED)) {

                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_ALREADY_EXISTS, request);
                return serverResponse;
            }

            var curUser = db.userTable.getUser(request.cookies().session.username);

            var game = db.gameTable.createGame(gameName, 2, GameStatus.PENDING, true, List.of(curUser));

            db.gameTable.addGame(game);

            serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request, ScreenUI.currentGame(gameName) + ScreenUI.GAME_PENDING_PROMPT);

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
                var games = db.gameTable.games.stream().filter(x -> x.status == GameStatus.PENDING).toList();

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

                var game = db.gameTable.games
                    .stream().filter(x -> x.name.equals(gameName) && x.status == GameStatus.PENDING)
                    .findFirst().orElse(null);

                if (db.gameTable.games
                    .stream().anyMatch(x -> x.name.equals(gameName) && x.status == GameStatus.IN_PROGRESS)) {

                    serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_EXISTS_BUT_NOT_PENDING, request);
                    return serverResponse;
                }

                if (game == null) {
                    serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_DOES_NOT_EXIST, request);
                    return serverResponse;
                }

                return joinGameResponse(request, game);
            }
        }
        else if (command.command().equals(CommandInfo.LIST_GAMES)) {
            //Validate arguments
            if (args.length != 0) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            var games = db.gameTable.games.stream().filter(x -> x.status != GameStatus.ENDED).toList();

            var message = ScreenUI.listGames(games);

            return messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message)
                    .setCookies(request.cookies())
            );
        }
//        else if (command.command().equals(CommandInfo.DELETE_GAME)) {
////            serverResponse = new ServerResponse(ResponseStatus.JOINING_GAME, null,
////                ScreenUI.PLACEHOLDER, request.session());
//        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            request.cookies().session.username = null;
            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request, ScreenUI.SUCCESSFUL_LOGOUT);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.LIST_GAMES,
                CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                CommandInfo.LOG_OUT, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }

    private ServerResponse joinGameResponse(ClientRequest request, Game game) {
        var curUser = db.userTable.getUser(request.cookies().session.username);

        game.status = GameStatus.IN_PROGRESS;
        game.addPlayer(curUser);

        //Go to the game screen
        request.cookies().session.currentScreen = ScreenInfo.GAME_SCREEN;

        List<PlayerCookie> playersCookies = new ArrayList<>();

        PlayerCookie curPlayerCookie = null;

        for (int i = 0; i < game.players.size(); i++) {
            var player = game.players.get(i);
            var playerCookie = new PlayerCookie(player.user.username(), i);

            if (playerCookie.player.equals(curUser.username())) {
                curPlayerCookie = playerCookie;
            }

            playersCookies.add(playerCookie);
        }

        var curClientGameCookie = new GameCookie(game.name,0, playersCookies);

        List<ServerResponse> signals = new ArrayList<>();

        var enemyPlayers = game.players.stream().filter(x -> !x.user.username().equals(curUser.username())).toList();

        for (var enemy : enemyPlayers) {
            var playerCookie = playersCookies.stream()
                .filter(x -> x.player.equals(enemy.user.username()))
                .findFirst().orElse(null);

            var enemyClientCookies = new ClientState(
                new SessionCookie(ScreenInfo.GAME_SCREEN, enemy.user.username()),
                playerCookie,
                new GameCookie(game.name,0, playersCookies)
            );

            var responseMessage = ScreenUI.GAME_FOUND_OPPONENT + ScreenUI.GAME_STARTING;

            var signalResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(responseMessage)
                    .setCookies(enemyClientCookies)
            );

            signals.add(signalResponse);
        }

        request.cookies().player = curPlayerCookie;
        request.cookies().game = curClientGameCookie;

        var serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request,
            ScreenUI.gameJoined(game.name) + ScreenUI.GAME_STARTING,
            signals);

        return serverResponse;
    }
}
