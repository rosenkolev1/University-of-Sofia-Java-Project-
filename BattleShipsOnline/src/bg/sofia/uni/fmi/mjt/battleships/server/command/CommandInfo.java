package bg.sofia.uni.fmi.mjt.battleships.server.command;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.QuitStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.dto.ListCommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandInfo {
    public static final String EXIT = "exit";
    public static final String HELP = "help";
    public static final String BACK = "back";

    public static final String REGISTER_CREDENTIALS = "<username> <password>";
    public static final String REGISTER = "register";
    public static final String REGISTER_SHORTHAND = "r";

    public static final String LOGIN = "login";
    public static final String LOGIN_SHORTHAND = "l";
    public static final String LOGIN_CREDENTIALS = "<username> <password>";

    public static final String LOG_OUT = "logout";

    public static final String GAME_NAME_ARG = "<game-name>";

    public static final String CREATE_GAME_VERBOSE = "create-game " + GAME_NAME_ARG;
    public static final String CREATE_GAME = "create-game";
    public static final String LIST_GAMES = "list-games";
    public static final String JOIN_GAME_VERBOSE = "join-game [" + GAME_NAME_ARG + "]";
    public static final String JOIN_GAME = "join-game";
    public static final String SAVED_GAMES = "saved-games";
    public static final String LOAD_GAME_VERBOSE = "load-game " + GAME_NAME_ARG;
    public static final String LOAD_GAME = "load-game";
    public static final String DELETE_GAME_VERBOSE = "delete-game " + GAME_NAME_ARG;
    public static final String DELETE_GAME = "delete-game";

    public static final String GAME_HIT_VERBOSE = "hit <rank><file>";
    public static final String GAME_HIT = "hit";
    public static final String GAME_ABANDON = "abandon";
    public static final String GAME_SAVE_AND_QUIT = "sq";

    public static final Map<String, ListCommandInfo> COMMAND_LIST_INFO_MAP = Map.of(
        CommandInfo.LIST_GAMES, new ListCommandInfo(
            (Game x, ClientRequest request) -> true,
            (List<Game> games) -> ScreenUI.listGames(games, ScreenUI.GAMES_LIST_EMPTY)),

        CommandInfo.SAVED_GAMES, new ListCommandInfo(
            (Game x, ClientRequest request) ->
                (x.status == GameStatus.PAUSED || (x.status == GameStatus.PENDING && x.quitStatus == QuitStatus.SAVE_AND_QUIT))
                    &&
                x.players.stream().map(y -> y.user.username()).anyMatch(y -> y.equals(request.cookies().session.username)),
            (List<Game> games) -> ScreenUI.listGames(games, ScreenUI.GAMES_SAVED_EMPTY))
        );

    public static final Map<QuitStatus, String> QUIT_STATUS_COMMAND_MAP = Map.of(
        QuitStatus.ABANDON, CommandInfo.GAME_ABANDON,
        QuitStatus.SAVE_AND_QUIT, CommandInfo.GAME_SAVE_AND_QUIT
    );

    public static final Map<String, QuitStatus> COMMAND_QUIT_STATUS_MAP =
        QUIT_STATUS_COMMAND_MAP.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
}
