package bg.sofia.uni.fmi.mjt.battleships.server.controller.game;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.GameCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.PlayerCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.screen.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.Command;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.*;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.ship.ShipStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.tile.TileStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.Player;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.PlayerStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.quit.QuitGameUI;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GameController extends Controller implements IGameController {

    public GameController(IDatabase db) {
        super(db);
    }

    @Override
    public ServerResponse respond(ClientRequest request) {
        ServerResponse serverResponse = null;

        var command = CommandCreator.newCommand(request.input());

        var game = db.gameTable().games()
            .stream().filter(x -> x.name.equals(request.cookies().game.name) &&
                !x.gameIsEndedOrDeleted()).findFirst()
            .get();

        var curPlayer = game.getPlayer(request.cookies().player.name);

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

        //Validate that the game can be quit (meaning that no other player has already )

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

        List<ServerResponse> signals = createSignalResponseUponQuit(request, game, curPlayer, game.quitStatus, quitGameUI);

        var message = new StringBuilder().append(quitGameUI.gameQuitCurrentUser());

        if (quitGame) {
            message.append(quitGameUI.gameEndingQuit());

            request.cookies().player = null;
            request.cookies().game = null;

            //Delete game or save game depending on the quitStatus
            if (quitStatus == QuitStatus.ABANDON) {
                game.status = GameStatus.ENDED;
                db.gameTable().saveGameFile(game);
            }
            else if (quitStatus == QuitStatus.SAVE_AND_QUIT) {
                game.status = GameStatus.PAUSED;
                db.gameTable().saveGameFile(game);
            }

            serverResponse = redirectResponse(
                ScreenInfo.HOME_SCREEN,
                ServerResponse
                    .builder()
                    .setCookies(request.cookies())
                    .setMessage(message.toString())
                    .setSignals(signals)
            );
        }
        else {
            message.append(quitGameUI.gameQuitWaiting());

            //Go to the next player and ask him if he wants to abandon the game!
            request.cookies().game.turn = game.turn;

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

        //Validate arguments
        if (args.length != 0) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        game.resumeGameFromQuitAttempt();

        List<ServerResponse> signals = createSignalResponsesUponQuitDenied(request, game, curPlayer, quitGameUI);

        request.cookies().game.turn = game.turn;
        request.cookies().game.quitPlayer = null;

        var message = new StringBuilder()
            .append(quitGameUI.gameQuitDeniedCurrentUser())
            .append(ScreenUI.GAME_RESUMING);

        serverResponse = messageResponse(
            ServerResponse
                .builder()
                .setCookies(request.cookies())
                .setMessage(message.toString())
                .setSignals(signals)
        );

        return serverResponse;
    }

    private ServerResponse respondHit(Command command, ClientRequest request, Game game, Player curPlayer) {
        ServerResponse serverResponse = null;

        var args = command.arguments();

        var gameHasThreeOrMorePlayers = game.alivePlayers().size() > 2;

        //Validate arguments
        if (!gameHasThreeOrMorePlayers && args.length != 1) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }
        else if (gameHasThreeOrMorePlayers && args.length != 2) {
            serverResponse = invalidCommandResponse(request);
            return serverResponse;
        }

        var targetTileString = args[0];

        var curUsername = request.cookies().session.username;

        String enemyName = null;

        if (!gameHasThreeOrMorePlayers) {
            enemyName = game.alivePlayers()
                .stream().filter(x -> !x.user.username().equals(curUsername))
                .findFirst().get().user.username();
        }
        else {
            enemyName = args[1];

            //Validate that the player name exists
            String finalEnemyName = enemyName;

            if (!game.players.stream().anyMatch(x ->
                x.user.username().equals(finalEnemyName) &&
                game.alivePlayers().contains(x))) {

                serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_HIT_TARGET_PLAYER_DOES_NOT_EXIST, request);
                return serverResponse;
            }

        }

        //Validate that you are not trying to attack yourself
        if (enemyName.equals(curUsername)) {
            serverResponse = invalidCommandResponse(ScreenUI.INVALID_GAME_HIT_TARGET_CANNOT_HIT_SELF, request);
            return serverResponse;
        }

        var enemyPlayer = game.getPlayer(enemyName);
        var enemyBoard = enemyPlayer.board;

        //DEBUG. IN THIS CASE, HIT ALL THE TILES AND MAKE THE GAME END INSTANTLY
        if (targetTileString.equals("all")) {
            var oldTurn = game.turn;

            targetTileString = "A1";
            for (var tile : enemyBoard.board()) {
                game.hitTile(curPlayer, enemyPlayer, tile.pos());
            }

            game.turn = oldTurn;
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

        game.hitTile(curUsername, enemyName, tilePos);

        var targetShip = enemyBoard.getShipForTile(tilePos);

        var hasHitShip = targetTile.status.equals(TileStatus.HIT_SHIP)
            && !oldTargetTileStatus.equals(TileStatus.HIT_SHIP);

        var hasSunkShip = targetShip != null && targetShip.status.equals(ShipStatus.SUNKEN)
            && !oldTargetTileStatus.equals(TileStatus.HIT_SHIP);

        var playerHasLost = enemyPlayer.status == PlayerStatus.DEAD;
        var gameHasEnded = game.status == GameStatus.ENDED;

        //Add the attacker's last move to the moves of the name cookie
        var curPlayerCookie = request.cookies().game.playersInfo.stream()
            .filter(x -> x.name.equals(curUsername)).findFirst().get();

        curPlayerCookie.moves.add(targetTileString);

        var finalEnemyName = enemyName;
        var enemyCookie = request.cookies().game.playersInfo.stream()
            .filter(x -> x.name.equals(finalEnemyName)).findFirst().get();

        List<ServerResponse> signals = createSignalResponsesUponHit(request, game, enemyCookie, targetTileString,
            hasHitShip, hasSunkShip, playerHasLost, gameHasEnded);

        //If the defender player has lost, then mark him as dead in the game cookie
        if (playerHasLost) {
            enemyCookie.playerStatusCode = PlayerStatus.DEAD.statusCode();
        }

        var message = ScreenUI.attackMessage(hasHitShip, hasSunkShip);

        //Check if the game has been won by the attacker
        if (gameHasEnded) {
            message.append("\n").append(ScreenUI.GAME_ENDING_WINNER);

            //Save the finished game to a file
            this.db.gameTable().saveGameFile(game);

            request.cookies().player = null;
            request.cookies().game = null;

            serverResponse = redirectResponse(ScreenInfo.HOME_SCREEN, request, message.toString(), signals);
        }
        else {
            //TODO: Right now, if the game has more than 2 players, player A will be able to tell on which tiles player B has hit player C.
            var enemyBoardWithFogOfWar = enemyBoard.boardWithFogOfWar();

            var curPlayerBoard = curPlayer.board;

            var curPlayerBoardString = curPlayerBoard.toString();
            var enemyBoardWithFogOfWarString = enemyBoardWithFogOfWar.toString();

            message.append("\n").append(ScreenUI.yourBoard(curPlayerBoardString))
                .append("\n").append(ScreenUI.enemyBoard(enemyBoardWithFogOfWarString)).append("\n");

            //Go to the next turn;
            request.cookies().game.turn = game.turn;

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

    private Predicate<PlayerCookie> filterAliveEnemyCookies(ClientRequest request) {
        return (PlayerCookie x) ->
            !x.name.equals(request.cookies().session.username) &&
            x.playerStatusCode == PlayerStatus.ALIVE.statusCode();
    }

    private List<ServerResponse> createSignalResponseUponQuit(ClientRequest request, Game curGame,
                                                              Player curPlayer, QuitStatus gameQuitStatus, QuitGameUI quitGameUI) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(filterAliveEnemyCookies(request)).toList();

        for (var enemy : enemies) {
            var cookies = new ClientState(
                new SessionCookie(null , enemy.name),
                null,
                null
            );

            var message = new StringBuilder()
                .append(quitGameUI.quitGame(curPlayer.user.username()));

            if (gameQuitStatus != QuitStatus.NONE) {
                message.append(quitGameUI.gameEndingQuit());
                cookies.session.currentScreen = ScreenInfo.HOME_SCREEN;
            }
            else {
                message.append(quitGameUI.gameQuitWaiting());
                cookies.session.currentScreen = ScreenInfo.GAME_SCREEN;

                cookies.player = enemy;

                cookies.game = new GameCookie(request.cookies().game);
                cookies.game.turn = curGame.turn;
            }

            signals.add(messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setCookies(cookies)
                    .build()
            ));
        }

        return signals;
    }

    private List<ServerResponse> createSignalResponsesUponQuitDenied(ClientRequest request, Game game, Player curPlayer, QuitGameUI quitGameUI) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(filterAliveEnemyCookies(request)).toList();

        for (var enemy : enemies) {
            var cookies = new ClientState(
                new SessionCookie(ScreenInfo.GAME_SCREEN , enemy.name),
                enemy,
                new GameCookie(request.cookies().game)
            );

            //Return the game's turn back to the player who first tried to abandon the game
            cookies.game.turn = game.turn;

            //Now remove the abandonPlayer from the cookie
            cookies.game.quitPlayer = null;

            var message = new StringBuilder()
                .append(quitGameUI.quitGameDenied(curPlayer.user.username()))
                .append(ScreenUI.GAME_RESUMING);

            signals.add(messageResponse(
                ServerResponse
                    .builder()
                    .setMessage(message.toString())
                    .setCookies(cookies)
                    .build()
            ));
        }

        return signals;
    }

    private List<ServerResponse> createSignalResponsesUponHit(ClientRequest request, Game game,
                                                              PlayerCookie defenderCookie, String tilePos,
                                                       boolean hasHitShip, boolean hasSunkShip,
                                                       boolean playerHasLost, boolean gameHasEnded) {
        List<ServerResponse> signals = new ArrayList<>();

        var enemies = request.cookies().game.playersInfo.stream()
            .filter(filterAliveEnemyCookies(request)).toList();

        if (playerHasLost) {
            defenderCookie.playerStatusCode = PlayerStatus.DEAD.statusCode();
        }

        var attackerName = request.cookies().session.username;
        var defenderEnemyName = defenderCookie.name;

        for (var enemy : enemies) {
            var isDefenderPlayer = enemy.name.equals(defenderEnemyName);

            var cookies = new ClientState(
                new SessionCookie(null, enemy.name),
                enemy,
                null
            );

            StringBuilder message = new StringBuilder();

            if (isDefenderPlayer && (gameHasEnded || playerHasLost)) {
                message
                    .append(ScreenUI.defendMessage(tilePos, hasHitShip, hasSunkShip))
                    .append(ScreenUI.GAME_ENDING_LOOSER);

                cookies.session.currentScreen = ScreenInfo.HOME_SCREEN;
                cookies.game = null;
                cookies.player = null;
            }
            else {
                message = enemy.name.equals(defenderEnemyName) ?
                    ScreenUI.defendMessage(tilePos, hasHitShip, hasSunkShip) :
                    ScreenUI.witnessMessage(attackerName, defenderEnemyName, tilePos, hasHitShip, hasSunkShip, playerHasLost);

                cookies.session.currentScreen = ScreenInfo.GAME_SCREEN;

                cookies.game = new GameCookie(request.cookies().game);
                cookies.game.turn = game.turn;
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
