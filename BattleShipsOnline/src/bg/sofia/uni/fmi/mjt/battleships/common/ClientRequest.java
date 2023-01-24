package bg.sofia.uni.fmi.mjt.battleships.common;

public record ClientRequest(String input, String currentScreen, String username) {

    public ClientRequest(String input, String currentScreen) {
        this(input, currentScreen, null);
    }
}
