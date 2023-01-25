package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.Objects;

public class SessionCookie {

    public String currentScreen;
    public String username;

    public SessionCookie(String currentScreen, String username) {
        this.currentScreen = currentScreen;
        this.username = username;
    }

    public SessionCookie(SessionCookie other) {
        this.currentScreen = other.currentScreen;
        this.username = other.username;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof SessionCookie)) {
            return false;
        }

        SessionCookie castOther = (SessionCookie) other;

        return Objects.equals(this.username, castOther.username) && Objects.equals(this.currentScreen, castOther.currentScreen);
    }
}
