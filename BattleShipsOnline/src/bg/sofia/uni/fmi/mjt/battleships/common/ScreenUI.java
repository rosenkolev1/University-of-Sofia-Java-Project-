package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.Arrays;

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
        """;

    public static final String INVALID_USERNAME_TAKEN = "\nThis username already exists! Choose a different one!";
    public static final String INVALID_PASSWORD_LENGTH = "\nInvalid password! The password should be at least 8 characters long!";
    public static final String INVALID_PASSWORD_TAKEN = "\nThis password already exists! Choose a different one!";

    public static final String SUCCESSFUL_REGISTRATION = "\nSuccessful registration! You can now log in to your account and start playing!";

    public static final String LOGIN_PROMPT = "\nPlease enter your username and password, seperated by a space in between:";

    public static final String INVALID_USER_DOES_NOT_EXIST = "\nA user with these username and password doesn't exist!";

    public static final String SUCCESSFUL_LOGIN = "\nSuccessful login!";

    public static final String HOME_PROMPT = "\nHello %s! You can start playing!";
    public static final String SUCCESSFUL_LOGOUT = "\nSuccessful logout!";

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

    public static String cleanText(String text) {
        while (text.contains("\n\n\n")) {
            text = text.replace("\n\n\n", "\n\n");
        }

        return text;
    }

    public static String invalidWithHelp(String invalidMessage) {
        return invalidMessage + HELP_PROMPT;
    }
}
