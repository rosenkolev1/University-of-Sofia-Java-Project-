package bg.sofia.uni.fmi.mjt.battleships.server.controller.home;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.GameCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.PlayerCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.screen.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;

import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.QuitStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.PlayerStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeController extends Controller implements IHomeController {

    public HomeController(IDatabase db) {
        super(db);
    }

    private ServerResponse validateCommandWithSingleArgumentGame(Command command, ClientRequest request) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

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

        return serverResponse;
    }

    private ServerResponse respondLoadGame(Command command, ClientRequest request) {
        ServerResponse serverResponse = validateCommandWithSingleArgumentGame(command, request);

        if (serverResponse != null) {
            return serverResponse;
        }

        var gameName = command.arguments()[0];

        var curUsername = request.cookies().session.username;

        //Validate that a paused game with the same name exists and that the current user is its creator
        if (!db.gameTable().games().stream()
            .anyMatch(x ->
                x.name.equals(gameName) &&
                x.status == GameStatus.PAUSED &&
                x.gameCreator().user.username().equals(curUsername)
            )) {

            serverResponse = invalidCommandResponse(ScreenUI.INVALID_SAVE_GAME_DOES_NOT_EXIST, request);
            return serverResponse;
        }

        var curUser = db.userTable().getUser(curUsername);

        var game = db.gameTable().games()
            .stream().filter(x -> x.name.equals(gameName) && x.status == GameStatus.PAUSED).findFirst().get();

        var curPlayer = game.players
            .stream().filter(x -> x.user.username().equals(curUser.username())).findFirst().get();

        //When reloading the game,
        game.resumeSavedGame(curPlayer);

        serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request, ScreenUI.currentGame(gameName) + ScreenUI.GAME_PENDING_PROMPT);

        return serverResponse;
    }

    private ServerResponse respondCreateGame(Command command, ClientRequest request) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length < 1 || args.length > 2) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var gameName = args[0];

        //Validate that the game name is valid
        if (gameName.isBlank()) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_NAME_NULL_EMPTY_BLANK, request);
            return serverResponse;
        }

        var playersCountArg = args.length == 2 ? args[1] : String.valueOf(Game.PLAYERS_CAPACITY_DEFAULT);

        if (!playersCountArg.matches("-?\\d+(\\.\\d+)?")) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_CREATE_GAME_INVALID_PLAYERS_COUNT, request);
            return serverResponse;
        }

        var playersCount = Integer.valueOf(playersCountArg);

        //Validate that an unfinished game with the same name has not been created already
        if (db.gameTable().games().stream()
            .anyMatch(x -> x.name.equals(gameName) &&
                !x.gameIsEndedOrDeleted())) {

            serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_ALREADY_EXISTS, request);
            return serverResponse;
        }

        var curUser = db.userTable().getUser(request.cookies().session.username);

        var game = db.gameTable().createGame(gameName, playersCount, GameStatus.PENDING, true, List.of(curUser));

        db.gameTable().addGame(game);

        serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request, ScreenUI.currentGame(gameName) + ScreenUI.GAME_PENDING_PROMPT);

        return serverResponse;
    }

    private ServerResponse respondJoinGame(Command command, ClientRequest request) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length > 1) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var curUsername = request.cookies().session.username;
        var gameName = args.length > 0 ? args[0] : null;

        //If there is no gameName, then choose a random game to join
        if (gameName == null) {

            var games = db.gameTable().games().stream().filter(x ->
                x.status == GameStatus.PENDING &&
                (x.quitStatus == QuitStatus.NONE || x.playerBelongsToSavedGame(curUsername))).toList();

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

            var savedGameNotYours = db.gameTable().games()
                .stream()
                .anyMatch(x ->
                    x.name.equals(gameName) &&
                    x.status == GameStatus.PENDING &&
                    x.quitStatus == QuitStatus.SAVE_AND_QUIT &&
                    !x.playerBelongsToSavedGame(curUsername)
                );

            var notSavedGameWithNameExists = db.gameTable().games()
                .stream()
                .anyMatch(x ->
                    x.name.equals(gameName) &&
                    x.status == GameStatus.PENDING &&
                    x.quitStatus == QuitStatus.NONE
                );

            if (savedGameNotYours && !notSavedGameWithNameExists) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_SAVED_GAME_NOT_YOURS, request);
                return serverResponse;
            }

            var game = db.gameTable().games()
                .stream().filter(x ->
                    x.name.equals(gameName) &&
                    x.status == GameStatus.PENDING &&
                    (x.quitStatus == QuitStatus.NONE || x.playerBelongsToSavedGame(curUsername))
                )
                .findFirst().orElse(null);

            if (db.gameTable().games()
                .stream().anyMatch(x -> x.name.equals(gameName) && x.status == GameStatus.IN_PROGRESS)) {

                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_EXISTS_BUT_IS_FULL, request);
                return serverResponse;
            }

            var pausedGameWithThisNameExists = db.gameTable().games().stream().anyMatch(x ->
                x.name.equals(gameName) &&
                x.status == GameStatus.PAUSED);

            if (pausedGameWithThisNameExists) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_EXISTS_BUT_IS_PAUSED, request);
                return serverResponse;
            }

            if (game == null) {
                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_DOES_NOT_EXIST, request);
                return serverResponse;
            }

            var isSavedGame = game.quitStatus == QuitStatus.SAVE_AND_QUIT;

            return joinGameResponse(request, game);
        }
    }

    private ServerResponse respondListGames(Command command, ClientRequest request) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length != 0) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var listCommandInfo = CommandInfo.COMMAND_LIST_INFO_MAP.get(command.command());

        var games = db.gameTable().games().stream().filter(x -> listCommandInfo.filter().test(x, request)).toList();
        var message = listCommandInfo.listFunction().apply(games);

        return messageResponse(
            ServerResponse
                .builder()
                .setMessage(message)
                .setCookies(request.cookies())
        );
    }

    private ServerResponse respondDeleteGame(Command command, ClientRequest request) {
        ServerResponse serverResponse = validateCommandWithSingleArgumentGame(command, request);

        if (serverResponse != null) {
            return serverResponse;
        }

        var curUsername = request.cookies().session.username;
        var gameName = command.arguments()[0];

        var game = this.db.gameTable().games().stream()
            .filter(x ->
                x.name.equals(gameName) &&
                x.status == GameStatus.PAUSED &&
                x.gameCreator().user.username().equals(curUsername))
            .findFirst().orElse(null);

        if (game == null) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_DELETE_GAME_DOES_NOT_EXISTS, request);
            return serverResponse;
        }

        //Set status and quitStatus of the game to Deleted and None
        this.db.gameTable().deleteGame(game);
        //Save the game as deleted in the table
        this.db.gameTable().saveGameFile(game);

        serverResponse = messageResponse(ServerResponse
            .builder()
            .setMessage(ScreenUI.DELETE_GAME_SUCCESS)
            .setCookies(request.cookies()));

        return serverResponse;
    }

    @Override
    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());
        var args = command.arguments();

        if (command.command().equals(CommandInfo.CREATE_GAME)) {
            serverResponse = respondCreateGame(command, request);
        }
        else if (command.command().equals(CommandInfo.JOIN_GAME)) {
            serverResponse = respondJoinGame(command, request);
        }
        else if (CommandInfo.COMMAND_LIST_INFO_MAP.containsKey(command.command())) {
            serverResponse = respondListGames(command, request);
        }
        else if (command.command().equals(CommandInfo.LOAD_GAME)) {
            serverResponse = respondLoadGame(command, request);
        }
        else if (command.command().equals(CommandInfo.DELETE_GAME)) {
            serverResponse = respondDeleteGame(command, request);
        }
        else if (request.input().equals(CommandInfo.LOG_OUT)) {
            request.cookies().session.username = null;
            serverResponse = redirectResponse(ScreenInfo.GUEST_HOME_SCREEN, request, ScreenUI.SUCCESSFUL_LOGOUT);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.LIST_GAMES,
                CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME_VERBOSE,
                CommandInfo.LOG_OUT, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }

    private ServerResponse joinGameResponse(ClientRequest request, Game game) {
        var curUser = db.userTable().getUser(request.cookies().session.username);

        //Resume the game if the current player had saved and quit before
        if (game.quitStatus == QuitStatus.SAVE_AND_QUIT) {
            var curPlayer = game.players.stream().filter(x -> x.user.username().equals(curUser.username())).findFirst().get();
            game.resumeSavedGame(curPlayer);
        }
        else {
            game.addPlayer(curUser);
        }

        var gameStarted = game.status == GameStatus.IN_PROGRESS;

        if (gameStarted) {
            //Go to the game screen
            request.cookies().session.currentScreen = ScreenInfo.GAME_SCREEN;

            List<PlayerCookie> playersCookies = new ArrayList<>();

            PlayerCookie curPlayerCookie = null;

            for (int i = 0; i < game.players.size(); i++) {
                var player = game.players.get(i);

                //Add all the previous moves from the turn history of the game for this player
                var playerPreviousMoves = game.turnHistory.stream()
                    .filter(x -> x.playerName().equals(player.user.username()))
                    .map(x -> x.turn()).toList();

                var playerCookie = new PlayerCookie(player.user.username(), i,
                    playerPreviousMoves, player.status.statusCode(), player.quitStatus.statusCode());

                if (playerCookie.name.equals(curUser.username())) {
                    curPlayerCookie = playerCookie;
                }

                playersCookies.add(playerCookie);
            }

            var curClientGameCookie = new GameCookie(game.name, game.turn, playersCookies);

            List<ServerResponse> signals = createSignalResponsesUponJoinAndStartGame(game, playersCookies, curUser);

            request.cookies().player = curPlayerCookie;
            request.cookies().game = curClientGameCookie;

            var serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request,
                ScreenUI.gameJoined(game.name) + ScreenUI.GAME_FILLED + ScreenUI.GAME_STARTING,
                signals);

            return serverResponse;
        }
        else {
            List<ServerResponse> signals = createSignalResponsesUponJoinGame(game, curUser);

            var serverResponse = redirectResponse(ScreenInfo.GAME_SCREEN, request,
                ScreenUI.gameJoined(game.name) + ScreenUI.GAME_PENDING_PROMPT,
                signals);

            return serverResponse;
        }
    }

    private List<ServerResponse> createSignalResponsesUponJoinGame(Game game, User curUser) {
        List<ServerResponse> signals = new ArrayList<>();

        //Send a messaged only to the enemies who have already joined the game before you
        var enemies = game.alivePlayers()
            .stream().filter(x ->
                x.quitStatus == QuitStatus.NONE &&
                    !x.user.username().equals(curUser.username()))
            .toList();

        for (var enemy : enemies) {
            var enemyResponseMessage = ScreenUI.opponentJoinedGame(curUser.username()) + ScreenUI.GAME_PENDING_PROMPT;

            var enemySignalResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(enemyResponseMessage)
                    .setCookies(new ClientState(new SessionCookie(ScreenInfo.GAME_SCREEN, enemy.user.username()), null, null))
            );

            signals.add(enemySignalResponse);
        }

        return signals;
    }

    private List<ServerResponse> createSignalResponsesUponJoinAndStartGame(Game game, List<PlayerCookie> playersCookies, User curUser) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemyPlayers = game.players.stream().filter(
            x -> x.status == PlayerStatus.ALIVE &&
                !x.user.username().equals(curUser.username())).toList();

        for (var enemy : enemyPlayers) {
            var playerCookie = playersCookies.stream()
                .filter(x -> x.name.equals(enemy.user.username()))
                .findFirst().orElse(null);

            var enemyClientCookies = new ClientState(
                new SessionCookie(ScreenInfo.GAME_SCREEN, enemy.user.username()),
                playerCookie,
                new GameCookie(game.name, game.turn, playersCookies)
            );

            var responseMessage = ScreenUI.opponentJoinedGame(curUser.username()) + ScreenUI.GAME_FILLED + ScreenUI.GAME_STARTING;

            var signalResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(responseMessage)
                    .setCookies(enemyClientCookies)
            );

            signals.add(signalResponse);
        }

        return signals;
    }
}
