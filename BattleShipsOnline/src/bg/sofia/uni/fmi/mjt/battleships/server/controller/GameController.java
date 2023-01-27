package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;

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

            var game = db.gameTable.getGame(request.game().name);

            var curUsername = request.session().username;
            var enemyName = request.game().playersInfo.stream()
                .filter(x -> !x.player.equals(curUsername))
                .findFirst().get().player;

            var enemyBoard = game.getPlayer(enemyName).board;

            //Validate that the argument is a valid tile
            if (!enemyBoard.validTilePos(targetTileString)) {
                serverResponse = invalidCommandResponse(ScreenUI.invalidHitTile(
                    enemyBoard.possibleRankValues(),
                    enemyBoard.possibleFileValues()
                ), request);

                return serverResponse;
            }

            var tilePos = enemyBoard.getTilePosFrom(targetTileString);
            var targetTile = enemyBoard.getTile(tilePos);

            var oldTargetTileStatus = targetTile.status;

            enemyBoard.hitTile(tilePos);

            var targetShip = enemyBoard.getShipForTile(tilePos);

            var hasHitShip = targetTile.status.equals(TileStatus.HIT_SHIP) && !oldTargetTileStatus.equals(TileStatus.HIT_SHIP);
            var hasSunkShip = targetShip != null && targetShip.status.equals(ShipStatus.SUNKEN);

            //Add the attacker's last move to the last move to the game cookie
            var curPlayerCookie = request.game().playersInfo.stream().filter(x -> x.player.equals(curUsername)).findFirst().get();
            curPlayerCookie.move = targetTileString;

            List<ServerResponse> signals = createSignalResponses(request, enemyName, targetTileString, hasHitShip, hasSunkShip);

            var message = attackerMessage(hasHitShip, hasSunkShip);

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

    private List<ServerResponse> createSignalResponses(ClientRequest request, String enemyName, String tilePos, boolean hasHitShip, boolean hasSunkShip) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.game().playersInfo.stream().filter(x -> !x.player.equals(request.session().username)).toList();

        for (var enemy : enemies) {
            var message = enemy.player.equals(enemyName) ?
                defenderMessage(tilePos, hasHitShip, hasSunkShip) :
                new StringBuilder(ScreenUI.PLACEHOLDER);

            var gameCookie = new GameCookie(request.game().name, -1, request.game().turn, request.game().playersInfo);
            gameCookie.nextTurn();

            var response = new ServerResponse(ResponseStatus.OK, null,
                message.toString(),
                new SessionCookie(null, enemy.player),
                gameCookie);

            signals.add(response);
        }

        return signals;
    }
}
