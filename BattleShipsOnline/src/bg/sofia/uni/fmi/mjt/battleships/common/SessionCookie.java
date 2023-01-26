package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.Objects;

public class SessionCookie {

    public String currentScreen;
    public String username;

    public SessionCookie(String currentScreen, String username) {
        this.currentScreen = currentScreen;
        this.username = username;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof SessionCookie castOther)) {
            return false;
        }

        return Objects.equals(this.username, castOther.username) && Objects.equals(this.currentScreen, castOther.currentScreen);
    }
}
