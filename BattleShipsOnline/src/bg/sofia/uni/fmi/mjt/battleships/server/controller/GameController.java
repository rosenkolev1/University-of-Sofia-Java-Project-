package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;
import net.bytebuddy.agent.VirtualMachine;

import java.util.ArrayList;
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

        if (command.command().equals(CommandInfo.GAME_HIT)) {
            //Validate arguments
            if (args.length != 1) {
                serverResponse = invalidCommandResponse(request);
                return serverResponse;
            }

            var targetTileString = args[0];

            var game = db.gameTable.getGame(request.game().name, GameStatus.IN_PROGRESS);

            var curUsername = request.session().username;
            var enemyName = request.game().playersInfo.stream()
                .filter(x -> !x.player.equals(curUsername))
                .findFirst().get().player;

            var enemyPlayer = game.getPlayer(enemyName);
            var enemyBoard = enemyPlayer.board;

            //DEBUG. IN THIS CASE, HIT ALL OF THE TILES AND MAKE THE GAME END INSTANTLY
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

            //Add the attacker's last move to the last move to the game cookie
            var curPlayerCookie = request.game().playersInfo.stream().filter(x -> x.player.equals(curUsername)).findFirst().get();
            curPlayerCookie.move = targetTileString;

            //Remove the dead players' last moves from the game cookie
            if (playerHasLost) {
                request.game().playersInfo = request.game().playersInfo.stream().filter(x -> x.player.equals(enemyName)).toList();
            }

            List<ServerResponse> signals = createSignalResponses(request, enemyName, targetTileString,
                hasHitShip, hasSunkShip, playerHasLost, gameHasEnded);

            var message = attackerMessage(hasHitShip, hasSunkShip);

            //Check if the game has been won by the attacker
            if (gameHasEnded) {
                message.append("\n").append(ScreenUI.GAME_ENDING_WINNER);
                request.session().currentScreen = ScreenInfo.HOME_SCREEN;

                //Finish the game and save it to the file
                this.db.gameTable.finishGame(game);

                serverResponse = new ServerResponse(ResponseStatus.FINISH_GAME, ScreenInfo.HOME_SCREEN,
                    message.toString(), request.session(), null, signals);
            }
            else {
                var enemyBoardWithFogOfWar = enemyBoard.boardWithFogOfWar();

                var curPlayer = game.getPlayer(curUsername);
                var curPlayerBoard = curPlayer.board;

                var curPlayerBoardString = curPlayerBoard.toString();
                var enemyBoardWithFogOfWarString = enemyBoardWithFogOfWar.toString();

                message.append("\n").append(ScreenUI.yourBoard(curPlayerBoardString))
                    .append("\n").append(ScreenUI.enemyBoard(enemyBoardWithFogOfWarString));

                //Go to the next turn;
                request.game().nextTurn();

                serverResponse = new ServerResponse(ResponseStatus.OK, null,
                    message.toString(), request.session(), request.game(), signals);
            }

        }
        else if (request.input().equals(CommandInfo.HELP)) {
            serverResponse = new ServerResponse(ResponseStatus.OK, null,
                ScreenUI.getAvailableCommands(
                    CommandInfo.GAME_HIT, CommandInfo.GAME_ABANDON, CommandInfo.GAME_SAVE_AND_QUIT,
                    CommandInfo.HELP), request.session(), request.game());
        }
        else {
            serverResponse = new ServerResponse(ResponseStatus.INVALID_COMMAND, null,
                ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), request.session(), request.game());
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

    private List<ServerResponse> createSignalResponses(ClientRequest request,
                                                       String enemyName, String tilePos,
                                                       boolean hasHitShip, boolean hasSunkShip,
                                                       boolean playerHasLost, boolean gameHasEnded) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.game().playersInfo.stream().filter(x -> !x.player.equals(request.session().username)).toList();

        for (var enemy : enemies) {
            var isDefenderPlayer = enemy.player.equals(enemyName);

            String redirect = null;
            ResponseStatus responseStatus = null;
            StringBuilder message = new StringBuilder();
            GameCookie gameCookie = null;
            SessionCookie sessionCookie = new SessionCookie(null, enemy.player);

            if (isDefenderPlayer && (gameHasEnded || playerHasLost)) {
                redirect = ScreenInfo.HOME_SCREEN;
                message
                    .append(defenderMessage(tilePos, hasHitShip, hasSunkShip))
                    .append("\n" + ScreenUI.GAME_ENDING_LOOSER);

                sessionCookie.currentScreen = ScreenInfo.HOME_SCREEN;

                responseStatus = ResponseStatus.FINISH_GAME;
            }
            else {
                message = enemy.player.equals(enemyName) ?
                    defenderMessage(tilePos, hasHitShip, hasSunkShip) :
                    new StringBuilder(ScreenUI.PLACEHOLDER);

                sessionCookie.currentScreen = ScreenInfo.GAME_SCREEN;

                gameCookie = new GameCookie(request.game().name, -1, request.game().turn, request.game().playersInfo);
                gameCookie.nextTurn();

                responseStatus = ResponseStatus.OK;
            }

            var response = new ServerResponse(responseStatus, redirect,
                message.toString(),
                sessionCookie,
                gameCookie);

            signals.add(response);
        }

        return signals;
    }
}
