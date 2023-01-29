package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.QuitGameUI;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Controller {
    private Database db;

    public GameController(Database db) {
        this.db = db;
    }

    private ServerResponse respondQuitGame(Command command, ClientRequest request, Game game, Player curPlayer, QuitStatus quitStatus) {
        var commandQuitStatus = CommandInfo.COMMAND_QUIT_STATUS_MAP.get(command.command());

        quitStatus = quitStatus == null || quitStatus == QuitStatus.NONE ? commandQuitStatus : quitStatus;

        QuitGameUI quitGameUI = ScreenUI.QUIT_STATUS_TO_UI_MAP.get(quitStatus);

        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length != 0) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        //Validate that the quit command is valid for the quitStatus
        if (quitStatus != null &&
            quitStatus != QuitStatus.NONE &&
            !quitStatus.equals(commandQuitStatus)) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        game.quitGame(curPlayer, quitStatus);

        boolean quitGame = game.quitStatus == quitStatus;

        request.cookies().player.quitStatusCode = quitStatus.statusCode();

        //In this case, the current player is the players who started the quit attempt
        if (request.cookies().game.quitPlayer == null) {
            request.cookies().game.quitPlayer = request.cookies().player;
        }

        List<ServerResponse> signals = createSignalResponseUponQuit(request, curPlayer, game.quitStatus, quitGameUI);

        var message = new StringBuilder().append(quitGameUI.gameQuitCurrentUser());

        if (quitGame) {
            message.append(quitGameUI.gameEndingQuit());

            request.cookies().player = null;
            request.cookies().game = null;

            //Delete game or save game depending on the quitStatus
            if (quitStatus == QuitStatus.ABANDON) {
                game.status = GameStatus.ENDED;
                db.gameTable.deleteGameFile(game);
            }
            else if (quitStatus == QuitStatus.SAVE_AND_QUIT) {
                game.status = GameStatus.PAUSED;
                db.gameTable.saveGameFile(game);
            }

            serverResponse = redirectResponse(
                ScreenInfo.HOME_SCREEN,
                ServerResponse
                    .builder()
                    .setStatus(ResponseStatus.QUIT_GAME)
                    .setCookies(request.cookies())
                    .setMessage(message.toString())
                    .setSignals(signals)
            );
        }
        else {
            message.append(quitGameUI.gameQuitWaiting());

            //Go to the next player and ask him if he wants to abandon the game!
            request.cookies().game.nextTurn();

            serverResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setCookies(request.cookies())
                    .setSignals(signals)
            );
        }

        return serverResponse;
    }

    private ServerResponse respondQuitGameDenied(Command command, ClientRequest request, Game game, Player curPlayer, QuitStatus quitStatus) {
        QuitGameUI quitGameUI = ScreenUI.QUIT_STATUS_TO_UI_MAP.get(quitStatus);

        ServerResponse serverResponse = null;

        var args = command.arguments();

        if (command.command().equals(CommandInfo.GAME_HIT)) {
            //Validate arguments
            if (args.length != 0) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            List<ServerResponse> signals = createSignalResponsesUponQuitDenied(request, curPlayer, quitGameUI);

            request.cookies().game.turn = request.cookies().game.quitPlayer.myTurn;
            request.cookies().game.quitPlayer = null;

            var message = new StringBuilder()
                .append(quitGameUI.gameQuitDeniedCurrentUser())
                .append(ScreenUI.GAME_RESUMING);

            game.resumeGame();

            serverResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setStatus(ResponseStatus.RESUME_GAME)
                    .setCookies(request.cookies())
                    .setMessage(message.toString())
                    .setSignals(signals)
            );
        }
        else if (command.command().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.GAME_HIT, CommandInfo.GAME_ABANDON, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }

    private ServerResponse respondHit(Command command, ClientRequest request, Game game, Player curPlayer) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length != 1) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var targetTileString = args[0];

        var curUsername = request.cookies().session.username;
        var enemyName = request.cookies().game.playersInfo.stream()
            .filter(x -> !x.player.equals(curUsername))
            .findFirst().get().player;

        var enemyPlayer = game.getPlayer(enemyName);
        var enemyBoard = enemyPlayer.board;

        //DEBUG. IN THIS CASE, HIT ALL THE TILES AND MAKE THE GAME END INSTANTLY
        if (targetTileString.equals("all")) {
            targetTileString = "A1";
            for (var tile : enemyBoard.board()) {
                game.hitTile(enemyPlayer, tile.pos());
            }
        }
        //Validate that the argument is a valid tile
        else if (!enemyBoard.validTilePos(targetTileString)) {
            serverResponse = invalidCommandResponse(ScreenUI.invalidHitTile(
                enemyBoard.possibleRankValues(),
                enemyBoard.possibleFileValues()
            ), request);

            return serverResponse;
        }

        var tilePos = enemyBoard.getTilePosFrom(targetTileString);
        var targetTile = enemyBoard.getTile(tilePos);

        var oldTargetTileStatus = targetTile.status;

        game.hitTile(enemyName, tilePos);

        var targetShip = enemyBoard.getShipForTile(tilePos);

        var hasHitShip = targetTile.status.equals(TileStatus.HIT_SHIP)
            && !oldTargetTileStatus.equals(TileStatus.HIT_SHIP);

        var hasSunkShip = targetShip != null && targetShip.status.equals(ShipStatus.SUNKEN)
            && !oldTargetTileStatus.equals(TileStatus.HIT_SHIP);

        var playerHasLost = enemyPlayer.status == PlayerStatus.DEAD;
        var gameHasEnded = game.status == GameStatus.ENDED;

        //Add the attacker's last move to the moves of the player cookie
        var curPlayerCookie = request.cookies().game.playersInfo.stream()
            .filter(x -> x.player.equals(curUsername)).findFirst().get();

        curPlayerCookie.moves.add(targetTileString);

        //Remove the dead players' from the game cookie
        if (playerHasLost) {
            request.cookies().game.playersInfo = request.cookies().game.playersInfo.stream()
                .filter(x -> x.player.equals(enemyName)).toList();
        }

        List<ServerResponse> signals = createSignalResponsesUponHit(request, enemyName, targetTileString,
            hasHitShip, hasSunkShip, playerHasLost, gameHasEnded);

        var message = attackerMessage(hasHitShip, hasSunkShip);

        //Check if the game has been won by the attacker
        if (gameHasEnded) {
            message.append("\n").append(ScreenUI.GAME_ENDING_WINNER);

            //Save the finished game to a file
            this.db.gameTable.saveGameFile(game);

            request.cookies().player = null;
            request.cookies().game = null;

            serverResponse = redirectResponse(ScreenInfo.HOME_SCREEN, request, message.toString(), signals);
        }
        else {
            var enemyBoardWithFogOfWar = enemyBoard.boardWithFogOfWar();

            var curPlayerBoard = curPlayer.board;

            var curPlayerBoardString = curPlayerBoard.toString();
            var enemyBoardWithFogOfWarString = enemyBoardWithFogOfWar.toString();

            message.append("\n").append(ScreenUI.yourBoard(curPlayerBoardString))
                .append("\n").append(ScreenUI.enemyBoard(enemyBoardWithFogOfWarString)).append("\n");

            //Go to the next turn;
            request.cookies().game.nextTurn();

            serverResponse = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setCookies(request.cookies())
                    .setSignals(signals)
            );
        }

        return serverResponse;
    }

    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());

        var game = db.gameTable.games
            .stream().filter(x -> x.name.equals(request.cookies().game.name) && x.status != GameStatus.ENDED).findFirst()
            .get();

        var curPlayer = game.getPlayer(request.cookies().player.player);

        var quitStatus = game.players.stream()
            .map(x -> x.quitStatus)
            .filter(x -> x != QuitStatus.NONE)
            .findFirst()
            .orElse(null);

        var tryingToQuit = quitStatus != null;

        if (tryingToQuit && command.command().equals(CommandInfo.GAME_HIT)) {
            serverResponse = respondQuitGameDenied(command, request, game, curPlayer, quitStatus);
        }
        else if (tryingToQuit && command.command().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.GAME_HIT, CommandInfo.QUIT_STATUS_COMMAND_MAP.get(quitStatus), CommandInfo.HELP);
        }
        else if (command.command().equals(CommandInfo.GAME_HIT)) {
            serverResponse = respondHit(command, request, game, curPlayer);
        }
        else if (CommandInfo.COMMAND_QUIT_STATUS_MAP.containsKey(command.command())) {
            serverResponse = respondQuitGame(command, request, game, curPlayer, quitStatus);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.GAME_HIT_VERBOSE, CommandInfo.GAME_ABANDON,
                CommandInfo.GAME_SAVE_AND_QUIT, CommandInfo.HELP);
        }
        else {
            serverResponse = invalidCommandResponse(request);
        }

        return serverResponse;
    }

    private StringBuilder defenderMessage(String tilePos, boolean hasHitShip, boolean hasSunkShip) {
        StringBuilder message = new StringBuilder();

        if (hasSunkShip) {
            message.append(ScreenUI.defenderShipSunk(tilePos));
        }
        else if (hasHitShip) {
            message.append(ScreenUI.defenderShipHit(tilePos));
        }
        else {
            message.append(ScreenUI.defenderHitMiss(tilePos));
        }

        return message;
    }

    private StringBuilder attackerMessage(boolean hasHitShip, boolean hasSunkShip) {
        StringBuilder message = new StringBuilder();

        if (!hasHitShip) {
            message.append(ScreenUI.GAME_TILE_HIT_MISS);
        }
        if (hasHitShip) {
            message.append(ScreenUI.GAME_TILE_HIT_SUCCESS);
        }
        if (hasSunkShip) {
            message.append(ScreenUI.GAME_SHIP_HIT_SUNK);
        }

        return message;
    }

    private List<ServerResponse> createSignalResponseUponQuit(ClientRequest request, Player curPlayer, QuitStatus gameQuitStatus, QuitGameUI quitGameUI) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(x -> !x.player.equals(request.cookies().session.username)).toList();

        for (var enemy : enemies) {
            ResponseStatus responseStatus = null;

            var cookies = new ClientState(
                new SessionCookie(null , enemy.player),
                enemy,
                null
            );

            var message = new StringBuilder()
                .append(quitGameUI.quitGame(curPlayer.user.username()));

            if (gameQuitStatus != QuitStatus.NONE) {
                message.append(quitGameUI.gameEndingQuit());
                cookies.session.currentScreen = ScreenInfo.HOME_SCREEN;
                responseStatus = ResponseStatus.QUIT_GAME;
            }
            else {
                message.append(quitGameUI.gameQuitWaiting());
                cookies.session.currentScreen = ScreenInfo.GAME_SCREEN;
                responseStatus = ResponseStatus.OK;

                cookies.game = new GameCookie(request.cookies().game);
                cookies.game.nextTurn();
            }

            signals.add(messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setStatus(responseStatus)
                    .setCookies(cookies)
                    .build()
            ));
        }

        return signals;
    }

    private List<ServerResponse> createSignalResponsesUponQuitDenied(ClientRequest request, Player curPlayer, QuitGameUI quitGameUI) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(x -> !x.player.equals(request.cookies().session.username)).toList();

        for (var enemy : enemies) {
            ResponseStatus responseStatus = ResponseStatus.RESUME_GAME;

            var cookies = new ClientState(
                new SessionCookie(ScreenInfo.GAME_SCREEN , enemy.player),
                enemy,
                new GameCookie(request.cookies().game)
            );

            //Return the game's turn back to the player who first tried to abandon the game
            cookies.game.turn = cookies.game.quitPlayer.myTurn;

            //Now remove the abandonPlayer from the cookie
            cookies.game.quitPlayer = null;

            var message = new StringBuilder()
                .append(quitGameUI.quitGameDenied(curPlayer.user.username()))
                .append(ScreenUI.GAME_RESUMING);

            signals.add(messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setStatus(responseStatus)
                    .setCookies(cookies)
                    .build()
            ));
        }

        return signals;
    }

    private List<ServerResponse> createSignalResponsesUponHit(ClientRequest request,
                                                       String enemyName, String tilePos,
                                                       boolean hasHitShip, boolean hasSunkShip,
                                                       boolean playerHasLost, boolean gameHasEnded) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(x -> !x.player.equals(request.cookies().session.username)).toList();

        for (var enemy : enemies) {
            var isDefenderPlayer = enemy.player.equals(enemyName);

            ResponseStatus responseStatus = null;
            var cookies = new ClientState(
                new SessionCookie(null, enemy.player),
                enemy,
                null
            );

            StringBuilder message = new StringBuilder();

            if (isDefenderPlayer && (gameHasEnded || playerHasLost)) {
                message
                    .append(defenderMessage(tilePos, hasHitShip, hasSunkShip))
                    .append(ScreenUI.GAME_ENDING_LOOSER);

                cookies.session.currentScreen = ScreenInfo.HOME_SCREEN;
                cookies.game = null;
                cookies.player = null;

                responseStatus = ResponseStatus.FINISH_GAME;
            }
            else {
                message = enemy.player.equals(enemyName) ?
                    defenderMessage(tilePos, hasHitShip, hasSunkShip) :
                    new StringBuilder(ScreenUI.PLACEHOLDER);

                cookies.session.currentScreen = ScreenInfo.GAME_SCREEN;

                cookies.game = new GameCookie(request.cookies().game);
                cookies.game.nextTurn();

                responseStatus = ResponseStatus.OK;
            }

            var response = messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setCookies(cookies)
            );

            signals.add(response);
        }

        return signals;
    }
}
