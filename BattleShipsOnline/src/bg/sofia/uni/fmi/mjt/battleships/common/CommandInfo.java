package bg.sofia.uni.fmi.mjt.battleships.common;

public class CommandInfo {
    public static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
        "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";

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

    public static final String CREATE_GAME = "create-game <game-name>";
    public static final String JOIN_GAME = "join-game [<game-name>]";
    public static final String SAVED_GAMES = "saved-games";
    public static final String LOAD_GAME = "load-game <game-name>";
    public static final String DELETE_GAME = "delete-game";

}
