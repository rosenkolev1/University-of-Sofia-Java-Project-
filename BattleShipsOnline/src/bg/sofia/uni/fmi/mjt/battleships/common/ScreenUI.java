package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.Arrays;
import java.util.List;

public class ScreenUI {
    public static final String PLACEHOLDER = "\nPLACEHOLDER STRING HERE";

    public static final String ENTER_COMMANDS_TEMPLATE = "\nEnter a command%s:";

    public static final String HELP_PROMPT = "\nType \"help\" to see all the available commands";

    public static final String AVAILABLE_COMMANDS_TEMPLATE = "\nAvailable commands:%s";

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

    public static final String INVALID_USERNAME_FORBIDDEN_CHARS = "\nThis username contains forbidden characters! Choose a different one!";
    public static final String INVALID_USERNAME_TAKEN = "\nThis username already exists! Choose a different one!";
    public static final String INVALID_PASSWORD_LENGTH = "\nInvalid password! The password should be at least 8 characters long!";
    public static final String INVALID_PASSWORD_FORBIDDEN_CHARS = "\nThis password contains forbidden characters! Choose a different one!";
    public static final String INVALID_PASSWORD_TAKEN = "\nThis password already exists! Choose a different one!";

    public static final String SUCCESSFUL_REGISTRATION = "\nSuccessful registration! You can now log in to your account and start playing!";

    public static final String LOGIN_PROMPT = "\nPlease enter your username and password, seperated by a space in between:";

    public static final String INVALID_USER_DOES_NOT_EXIST = "\nA user with these username and password doesn't exist!";
    public static final String INVALID_USER_ALREADY_LOGGED_IN = "\nThis user has already logged in!";

    public static final String SUCCESSFUL_LOGIN = "\nSuccessful login!";

    public static final String HOME_PROMPT = "\nHello %s! You can start playing!";

    public static final String INVALID_GAME_NAME_NULL_EMPTY_BLANK = "\nThe name of the game is null, empty or blank!";
    public static final String INVALID_GAME_ALREADY_EXISTS = "\nA game with this name already exists! Choose a different name!";
    public static final String INVALID_NO_GAMES_AVAILABLE = "\nThere are no pending games available at this time!";

    public static final String SUCCESSFUL_LOGOUT = "\nSuccessful logout!";

    public static final String CURRENT_GAME_TEMPLATE = "\nCurrent game room: %s";
    public static final String GAME_PENDING_PROMPT = "\nCurrently waiting for a second enemy...";
    public static final String GAME_FOUND_OPPONENT = "\nAn opponent has been found!";
    public static final String GAME_STARTING = "\nGame is starting...";
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

    public static final String GAME_DEFENDER_HIT_MISSED_TEMPLATE = "\nYour attacker missed on tile \"%s\" :)";
    public static final String GAME_DEFENDER_SHIP_HIT_TEMPLATE = "\nYour ship on tile \"%s\" has been hit :(";
    public static final String GAME_DEFENDER_SHIP_SUNK_TEMPLATE = "\nYour ship on tile \"%s\" has been hit and it has sunk :( :(";

    public static final String GAME_YOUR_BOARD = "YOUR BOARD\n";
    public static final String GAME_ENEMY_BOARD = "ENEMY BOARD\n";

    public static final String GAME_ENDING_RETURN_TO_MAIN = "\nReturning to main menu...";
    public static final String GAME_ENDING_WINNER = "\nYou have won the game! Congrats :)" + GAME_ENDING_RETURN_TO_MAIN;
    public static final String GAME_ENDING_LOOSER = "\nYou have lost the game! That's unfortunate :(" + GAME_ENDING_RETURN_TO_MAIN;

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
                .filter(x -> x.move != null)
                .map(x -> String.format(GAME_ENEMY_LAST_TURN_TEMPLATE, x.player, x.move)).toList())
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
