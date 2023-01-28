package bg.sofia.uni.fmi.mjt.battleships.server.ui;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.PlayerCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.QuitStatus;

import java.util.*;
import java.util.function.Function;

public class ScreenUI {
    public static final String PLACEHOLDER = "\nPLACEHOLDER STRING HERE";

    public static final String ENTER_COMMANDS_TEMPLATE = "\nEnter a command%s:";

    public static final String HELP_PROMPT = "\nType \"help\" to see all the available commands\n";

    public static final String AVAILABLE_COMMANDS_TEMPLATE = "\nAvailable commands:%s\n";

    public static final String INVALID_COMMAND = "\nInvalid command, try again!";

    public static final String EXIT_SUCCESS = "\nSaving and exiting the game!";

    public static final String REDIRECT_TEMPLATE = "\nRedirecting from %s to %s!";

    public static final String GUEST_HOME_PROMPT =
        """
        \nWelcome to BattleShips Online!
        To start playing, please log in.
        Don't have an account? Then register now and start playing!
        Want to exit the game? Type "exit" to stop playing!
        Want to go back one screen? Type "back" (currently disabled because this is the home screen).
        Confused about what commands you can input? Type "help" at any point!
        """ + enterCommandPrompt(
            CommandInfo.REGISTER, CommandInfo.REGISTER_SHORTHAND,
            CommandInfo.LOGIN, CommandInfo.LOGIN_SHORTHAND,
            CommandInfo.EXIT, CommandInfo.HELP);

    public static final String REGISTER_PROMPT =
        """
        \nPlease register by entering your username and password, seperated by a space in between:
        Password should be at least 8 characters long and cannot contain whitespaces! 
        """ + "The username and password can only contain numbers, letters and underscores";

    public static final String INVALID_USERNAME_FORBIDDEN_CHARS = "\nThis username contains forbidden characters! Choose a different one!\n";
    public static final String INVALID_USERNAME_TAKEN = "\nThis username already exists! Choose a different one!\n";
    public static final String INVALID_PASSWORD_FORBIDDEN_CHARS = "\nThis password contains forbidden characters! Choose a different one!\n";
    public static final String INVALID_PASSWORD_LENGTH = "\nInvalid password! The password should be at least 8 characters long!\n";
    public static final String INVALID_PASSWORD_TAKEN = "\nThis password already exists! Choose a different one!\n";

    public static final String SUCCESSFUL_REGISTRATION = "\nSuccessful registration! You can now log in to your account and start playing!\n";

    public static final String LOGIN_PROMPT = "\nPlease enter your username and password, seperated by a space in between:";

    public static final String INVALID_USER_DOES_NOT_EXIST = "\nA user with these username and password doesn't exist!\n";
    public static final String INVALID_USER_ALREADY_LOGGED_IN = "\nThis user has already logged in!\n";

    public static final String SUCCESSFUL_LOGIN = "\nSuccessful login!\n";

    public static final String HOME_PROMPT = "\nHello %s! You can start playing!";

    public static final String INVALID_GAME_NAME_NULL_EMPTY_BLANK = "\nInvalid name! The name of the game cannot be null, empty or blank!\n";
    public static final String INVALID_GAME_ALREADY_EXISTS = "\nA game with this name already exists! Choose a different name!\n";
    public static final String INVALID_GAME_DOES_NOT_EXIST = "\nA game with this name does not exist!\n";
    public static final String INVALID_GAME_EXISTS_BUT_NOT_PENDING = "\nThe game with this name is full!\n";
    public static final String INVALID_NO_GAMES_AVAILABLE = "\nThere are no pending games available at this time!\n";

    public static final String GAMES_LIST_EMPTY = "\nThere are currently no games!\n";
    public static final String GAMES_LIST_HEADER_NAME = "NAME";
    public static final String GAMES_LIST_HEADER_CREATOR = "CREATOR";
    public static final String GAMES_LIST_HEADER_STATUS = "STATUS";
    public static final String GAMES_LIST_HEADER_PLAYERS = "PLAYERS";
    public static final List<String> GAMES_LIST_HEADERS = List.of(
        GAMES_LIST_HEADER_NAME,
        GAMES_LIST_HEADER_CREATOR,
        GAMES_LIST_HEADER_STATUS,
        GAMES_LIST_HEADER_PLAYERS
    );

    public static final String GAMES_LIST_HEADER_SEPARATOR = "-";
    public static final String GAMES_LIST_HEADER_SEPARATOR_LINE_TEMPLATE = "|%s+%s+%s+%s|";
    public static final String GAMES_LIST_PLAYERS = "%s/%s";
    public static final String GAMES_LIST_ROW_TEMPLATE = "| %s | %s | %s | %s |";

    public static final String SUCCESSFUL_LOGOUT = "\nSuccessful logout!\n";

    public static final String CURRENT_GAME_TEMPLATE = "\nCurrent game room: %s";
    public static final String GAME_PENDING_PROMPT = "\nCurrently waiting for a second enemy...\n";
    public static final String GAME_FOUND_OPPONENT = "\nAn opponent has been found!";
    public static final String GAME_STARTING = "\nGame is starting...\n";
    public static final String GAME_JOINED_TEMPLATE = "\nJoined game \"%s\"";
    public static final String GAME_ENEMY_LAST_TURN_TEMPLATE = "\n%s's last turn: %s";
    public static final String GAME_MY_TURN = "\nIt's your turn now! Enter your turn:";
    public static final String GAME_ENEMY_TURN_TEMPLATE = "\nWaiting for %s's turn now!";

    public static final String INVALID_GAME_HIT_TILE_TEMPLATE =
        """

        The coordinates for the tile are incorrect!
        Possible rank values: %s
        Possible file values: %s
        
        """;

    public static final String GAME_TILE_HIT_MISS = "\nYour attack missed :(";
    public static final String GAME_TILE_HIT_SUCCESS = "\nSuccessful hit!";
    public static final String GAME_SHIP_HIT_SUNK = " You have managed to sink a ship! Good job!";

    public static final String GAME_DEFENDER_HIT_MISSED_TEMPLATE = "\nYour attacker missed on tile \"%s\" :)\n";
    public static final String GAME_DEFENDER_SHIP_HIT_TEMPLATE = "\nYour ship on tile \"%s\" has been hit :(\n";
    public static final String GAME_DEFENDER_SHIP_SUNK_TEMPLATE = "\nYour ship on tile \"%s\" has been hit and it has sunk :( :(\n";

    public static final String GAME_YOUR_BOARD = "YOUR BOARD\n";
    public static final String GAME_ENEMY_BOARD = "ENEMY BOARD\n";

    public static final String GAME_RESUMING = "\nResuming the game!\n";

    public static final String GAME_ENDING_RETURN_TO_MAIN = "\nReturning to main menu...\n";
    public static final String GAME_ENDING_WINNER = "\nYou have won the game! Congrats :)" + GAME_ENDING_RETURN_TO_MAIN;
    public static final String GAME_ENDING_LOOSER = "\nYou have lost the game! That's unfortunate :(" + GAME_ENDING_RETURN_TO_MAIN;

    public static final Map<QuitStatus, QuitGameUI> QUIT_STATUS_TO_UI_MAP = Map.of(
        QuitStatus.ABANDON, new AbandonGameUI(),
        QuitStatus.SAVE_AND_QUIT, new SaveAndQuitGameUI()
    );

    public static final Map<String, Function<ClientState, String>> SCREENS_PROMPTS = Map.of(
        ScreenInfo.GUEST_HOME_SCREEN, (cookies) -> ScreenUI.GUEST_HOME_PROMPT,
        ScreenInfo.REGISTER_SCREEN, (cookies) -> ScreenUI.REGISTER_PROMPT,
        ScreenInfo.LOGIN_SCREEN, (request) -> ScreenUI.LOGIN_PROMPT,

        ScreenInfo.HOME_SCREEN, (cookies) -> ScreenUI.homePrompt(cookies.session.username) +
            ScreenUI.getAvailableCommands(
                CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.LIST_GAMES,
                CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                CommandInfo.LOG_OUT, CommandInfo.HELP
            ) + ScreenUI.enterCommandPrompt(),

        ScreenInfo.GAME_SCREEN, (cookies) -> {
            if (cookies.game == null) {
                return null;
            }
            else if (cookies.game.turn == cookies.player.myTurn) {
                //In this case, a vote for quitting the game has started
                if (cookies.game.quitPlayer != null) {
                    QuitGameUI quitGameUI = QUIT_STATUS_TO_UI_MAP.get(QuitStatus.getByCode(cookies.game.quitPlayer.quitStatusCode));

                    return quitGameUI.gameMyTurnQuitPrompt();
                }
                else {
                    return ScreenUI.myTurnPrompt(cookies.game.playersInfo);
                }
            }
            else {
                return ScreenUI.enemyTurnPrompt(cookies.game.playersInfo.get(cookies.game.turn).player);
            }
        }
    );

    public static String redirectMessage(String from, String to) {
        return String.format(REDIRECT_TEMPLATE, from, to);
    }

    public static String enterCommandPrompt(String... commands) {
        if (commands.length > 0) {
            return String.format(ENTER_COMMANDS_TEMPLATE, " (" + String.join("/", commands) + ")");
        }

        return String.format(ENTER_COMMANDS_TEMPLATE, "");
    }

    public static String getAvailableCommands(String... commands) {
        var res = String.format(AVAILABLE_COMMANDS_TEMPLATE, String.join("", Arrays.stream(commands).map(x -> "\n       " + x).toList()));

        return res;
    }

    public static String homePrompt(String username) {
        return String.format(HOME_PROMPT, username);
    }

    public static String gameJoined(String name) {
        return String.format(GAME_JOINED_TEMPLATE, name);
    }

    public static String myTurnPrompt(List<PlayerCookie> enemyInfo) {
        return String.join("", enemyInfo.stream()
                .filter(x -> x.moves != null && !x.moves.isEmpty())
                .map(x -> String.format(GAME_ENEMY_LAST_TURN_TEMPLATE, x.player, x.moves.get(x.moves.size() - 1))).toList())
            + GAME_MY_TURN;
    }

    public static String enemyTurnPrompt(String enemy) {
        return String.format(GAME_ENEMY_TURN_TEMPLATE, enemy);
    }

    public static String invalidHitTile(List<String> ranks, List<String> files) {
        return String.format(INVALID_GAME_HIT_TILE_TEMPLATE,
            String.join(", ", ranks), String.join(", ", files));
    }

    public static String currentGame(String gameName) {
        var res = String.format(CURRENT_GAME_TEMPLATE, gameName);
        return res;
    }

    public static String defenderHitMiss(String tilePos) {
        return String.format(GAME_DEFENDER_HIT_MISSED_TEMPLATE, tilePos);
    }

    public static String defenderShipHit(String tilePos) {
        return String.format(GAME_DEFENDER_SHIP_HIT_TEMPLATE, tilePos);
    }

    public static String defenderShipSunk(String tilePos) {
        return String.format(GAME_DEFENDER_SHIP_SUNK_TEMPLATE, tilePos);
    }

    public static String enemyBoard(String board) {
        return boardWithAnnotation(GAME_ENEMY_BOARD, board);
    }

    public static String yourBoard(String board) {
        return boardWithAnnotation(GAME_YOUR_BOARD, board);
    }

    public static String listGames(List<Game> games) {
        if (games == null || games.size() == 0) {
            return ScreenUI.GAMES_LIST_EMPTY;
        }

        Function<Game, String> nameMapper = (Game x) -> x.name;
        Function<Game, String> creatorMapper = (Game x) -> x.players.get(0).user.username();
        Function<Game, String> statusMapper = (Game x) -> x.status.status();
        Function<Game, String> playersMapper = (Game x) -> String.format(GAMES_LIST_PLAYERS, x.players.size(), x.playerCount);

        List<Function<Game, String>> fieldMappers = List.of(
            nameMapper,
            creatorMapper,
            statusMapper,
            playersMapper
        );

        List<Integer> longestFields = new ArrayList<>();

        for (var fieldMapper : fieldMappers) {
            var longestField = games.stream().map(fieldMapper).max(Comparator.comparingInt(String::length)).get().length();
            longestFields.add(longestField);
        }

        StringBuilder table = new StringBuilder();

        //Create the headers
        List<String> headers = new ArrayList<>();
        List<String> headerLineSeparators = new ArrayList<>();

        for (int i = 0; i < GAMES_LIST_HEADERS.size(); i++) {
            var header = GAMES_LIST_HEADERS.get(i);
            var headerIsLonger = header.length() > longestFields.get(i);
            var lengthDiff = longestFields.get(i) - header.length();

            if (headerIsLonger) {
                longestFields.set(i, header.length());
            }

            var headerWithSpaces = headerIsLonger ? header : header + " ".repeat(lengthDiff);
            headers.add(headerWithSpaces);

            var headerLineSeparator = GAMES_LIST_HEADER_SEPARATOR.repeat(2 + longestFields.get(i));
            headerLineSeparators.add(headerLineSeparator);
        }

        table.append(String.format(GAMES_LIST_ROW_TEMPLATE, headers.toArray())).append("\n");
        table.append(String.format(GAMES_LIST_HEADER_SEPARATOR_LINE_TEMPLATE, headerLineSeparators.toArray())).append("\n");

        //Add all the rows for the games
        for (var game : games) {
            List<String> rowFields = new ArrayList<>();

            for (int i = 0; i < fieldMappers.size(); i++) {
                var fieldMapper = fieldMappers.get(i);

                var field = fieldMapper.apply(game);
                var longestField = longestFields.get(i);

                var fieldWithSpaces = field.length() < longestField ?
                    field + " ".repeat(longestField - field.length()) :
                    field;

                rowFields.add(fieldWithSpaces);
            }

            table.append(String.format(GAMES_LIST_ROW_TEMPLATE, rowFields.toArray())).append("\n");
        }

        return table.toString();
    }

    public static String cleanText(String text) {
        while (text.contains("\n\n\n")) {
            text = text.replace("\n\n\n", "\n\n");
        }

        return text;
    }

    public static String invalidWithHelp(String invalidMessage) {
        return invalidMessage + HELP_PROMPT;
    }

    private static String boardWithAnnotation(String annotation, String board) {
        var rowLength = board.indexOf("\n");

        var rowBeginningWhitespaces = 0;

        while(board.substring(0, rowLength).startsWith(" ".repeat(rowBeginningWhitespaces))) {
            rowBeginningWhitespaces++;
        }

        rowBeginningWhitespaces-=2;

        rowLength -= rowBeginningWhitespaces;

        var annotationLength = annotation.strip().length();
        int whiteSpace = 0;

        while(Math.abs(whiteSpace - (rowLength - (whiteSpace + annotationLength))) >= 2) {
            whiteSpace++;
        }

        return "\n" + " ".repeat(rowBeginningWhitespaces) + " ".repeat(whiteSpace) + annotation + board;
    }
}
