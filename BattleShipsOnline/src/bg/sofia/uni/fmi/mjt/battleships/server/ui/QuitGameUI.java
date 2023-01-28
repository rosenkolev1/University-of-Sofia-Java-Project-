package bg.sofia.uni.fmi.mjt.battleships.server.ui;

public abstract class QuitGameUI {
    public abstract String gameMyTurnQuitPrompt();
    public abstract String gameQuitCurrentUser();
    public abstract String gameQuitWaiting();
    public abstract String gameQuitTemplate();
    public abstract String gameQuitDeniedTemplate();
    public abstract String gameQuitDeniedCurrentUser();
    
    public abstract String gameEndingQuit();
    
    public String quitGameDenied(String user) {
        return String.format(gameQuitDeniedTemplate(), user);
    }

    public String quitGame(String user) {
        return String.format(gameQuitTemplate(), user);
    }
    
}
