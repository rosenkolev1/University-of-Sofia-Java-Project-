package bg.sofia.uni.fmi.mjt.battleships.common;

import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandExecutor;

import java.util.List;
import java.util.Map;

public class ScreenInfo {
    public static final String GUEST_HOME_SCREEN = "guestHomeScreen";
    public static final String REGISTER_SCREEN = "registerScreen";
    public static final String LOGIN_SCREEN = "loginScreen";
    public static final String HOME_SCREEN = "homeScreen";

    public static final String[] SCREEN_NAMES = {
        GUEST_HOME_SCREEN,
        LOGIN_SCREEN,
        HOME_SCREEN,
    };

    private static final List<ScreenRedirect> SCREEN_REDIRECTS = List.of(
        new ScreenRedirect(GUEST_HOME_SCREEN, LOGIN_SCREEN),
        new ScreenRedirect(GUEST_HOME_SCREEN, REGISTER_SCREEN),
        new ScreenRedirect(REGISTER_SCREEN, GUEST_HOME_SCREEN),
        new ScreenRedirect(LOGIN_SCREEN, GUEST_HOME_SCREEN),
        new ScreenRedirect(LOGIN_SCREEN, HOME_SCREEN),
        new ScreenRedirect(HOME_SCREEN, GUEST_HOME_SCREEN)
    );

    public static boolean validRedirect(String from, String to) {
        var desiredRedirect = new ScreenRedirect(from, to);

        return SCREEN_REDIRECTS.stream().anyMatch(x -> x.equals(desiredRedirect));
    }


}
