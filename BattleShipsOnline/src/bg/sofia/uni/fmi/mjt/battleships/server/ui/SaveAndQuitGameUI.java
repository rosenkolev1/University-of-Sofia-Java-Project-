package bg.sofia.uni.fmi.mjt.battleships.server.ui;

public class SaveAndQuitGameUI extends QuitGameUI {
    private static final String GAME_SAVE_AND_QUIT_CURRENT_USER = "\nYou have decided to save and quit the game!";
    private static final String GAME_SAVE_AND_QUIT_WAITING = "\nWaiting for other users to save and quit the game!\n";
    private static final String GAME_SAVE_AND_QUIT_TEMPLATE = "\n%s has decided to save and quit the game!";
    private static final String GAME_SAVE_AND_QUIT_DENIED_TEMPLATE = "\n%s has decided to not save and quit the game";
    private static final String GAME_SAVE_AND_QUIT_DENIED_CURRENT_USER = "\nYou have decided to not save and quit the game!";

    private static final String GAME_ENDING_SAVE_AND_QUIT = "\nThe current game has been saved and quit by all players!\n" + ScreenUI.GAME_ENDING_RETURN_TO_MAIN;

    public static final String GAME_MY_TURN_SAVE_AND_QUIT_PROMPT =
        """
        
        It's your turn now! You have a choice between resuming the game (hit) or saving and quitting the game(sq)
        Enter your turn:""";

    @Override
    public String gameMyTurnQuitPrompt() {
        return GAME_MY_TURN_SAVE_AND_QUIT_PROMPT;
    }

    @Override
    public String gameQuitCurrentUser() {
        return GAME_SAVE_AND_QUIT_CURRENT_USER;
    }

    @Override
    public String gameQuitWaiting() {
        return GAME_SAVE_AND_QUIT_WAITING;
    }

    @Override
    public String gameQuitTemplate() {
        return GAME_SAVE_AND_QUIT_TEMPLATE;
    }

    @Override
    public String gameQuitDeniedTemplate() {
        return GAME_SAVE_AND_QUIT_DENIED_TEMPLATE;
    }

    @Override
    public String gameQuitDeniedCurrentUser() {
        return GAME_SAVE_AND_QUIT_DENIED_CURRENT_USER;
    }

    @Override
    public String gameEndingQuit() {
        return GAME_ENDING_SAVE_AND_QUIT;
    }
}
