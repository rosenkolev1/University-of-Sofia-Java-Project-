package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Controller {
    private Database db;

    public GameController(Database db) {
        this.db = db;
    }

    public ServerResponse respondAbandonDenied(Command command, ClientRequest request, Game game, Player curPlayer) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        if (command.command().equals(CommandInfo.GAME_HIT)) {
            //Validate arguments
            if (args.length != 0) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            List<ServerResponse> signals = createSignalResponsesUponAbandonDenied(request, curPlayer);

            request.cookies().game.turn = request.cookies().game.abandonPlayer.myTurn;
            request.cookies().game.abandonPlayer = null;

            var message = new StringBuilder()
                .append(ScreenUI.GAME_ABANDON_DENIED_CURRENT_USER)
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

    public ServerResponse respondAbandon(Command command, ClientRequest request, Game game, Player curPlayer) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        //Validate arguments
        if (args.length != 0) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        game.abandonGame(curPlayer);

        var abandonGame = game.status == GameStatus.ABANDONED;

        //In this case, the current player is the players who starts the abandon attempt
        if (request.cookies().game.abandonPlayer == null) {
            request.cookies().game.abandonPlayer = request.cookies().player;
        }

        List<ServerResponse> signals = createSignalResponseUponAbandon(request, curPlayer, abandonGame);

        var message = new StringBuilder().append(ScreenUI.GAME_ABANDON_CURRENT_USER);

        if (abandonGame) {
            message.append(ScreenUI.GAME_ENDING_ABANDONED);

            request.cookies().player = null;
            request.cookies().game = null;

            db.gameTable.deleteGame(game);

            serverResponse = redirectResponse(
                ScreenInfo.HOME_SCREEN,
                ServerResponse
                    .builder()
                    .setStatus(ResponseStatus.ABANDON_GAME)
                    .setCookies(request.cookies())
                    .setMessage(message.toString())
                    .setSignals(signals)
            );
        }
        else {
            message.append(ScreenUI.GAME_ABANDON_WAITING);

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

    public ServerResponse respondHit(Command command, ClientRequest request, Game game, Player curPlayer) {
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

        var tryingToAbandon = game.players.stream().anyMatch(x -> x.status == PlayerStatus.ABANDON);

        if (tryingToAbandon && command.command().equals(CommandInfo.GAME_HIT)) {
            serverResponse = respondAbandonDenied(command, request, game, curPlayer);
        }
        else if (command.command().equals(CommandInfo.GAME_HIT)) {
            serverResponse = respondHit(command, request, game, curPlayer);
        }
        else if (command.command().equals(CommandInfo.GAME_ABANDON)) {
            serverResponse = respondAbandon(command, request, game, curPlayer);
        }
        else if (tryingToAbandon && command.command().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.GAME_HIT, CommandInfo.GAME_ABANDON, CommandInfo.HELP);
        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = helpResponse(request,
                CommandInfo.GAME_HIT, CommandInfo.GAME_ABANDON,
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

    private List<ServerResponse> createSignalResponseUponAbandon(ClientRequest request, Player curPlayer, boolean abandonGame) {
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
                .append(ScreenUI.abandonGame(curPlayer.user.username()));

            if (abandonGame) {
                message.append(ScreenUI.GAME_ENDING_ABANDONED);
                cookies.session.currentScreen = ScreenInfo.HOME_SCREEN;
                responseStatus = ResponseStatus.ABANDON_GAME;
            }
            else {
                message.append(ScreenUI.GAME_ABANDON_WAITING);
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

    private List<ServerResponse> createSignalResponsesUponAbandonDenied(ClientRequest request, Player curPlayer) {
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
            cookies.game.turn = cookies.game.abandonPlayer.myTurn;

            //Now remove the abandonPlayer from the cookie
            cookies.game.abandonPlayer = null;

            var message = new StringBuilder()
                .append(ScreenUI.abandonGameDenied(curPlayer.user.username()))
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
