package bg.sofia.uni.fmi.mjt.battleships.server.ui.quit;

import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

public class AbandonGameUI extends QuitGameUI {
    private static final String GAME_ABANDON_CURRENT_USER = "\nYou have decided to abandon the game!";
    private static final String GAME_ABANDON_WAITING = "\nWaiting for other users to abandon the game!\n";
    private static final String GAME_ABANDON_TEMPLATE = "\n%s has decided to abandon the game!";
    private static final String GAME_ABANDON_DENIED_TEMPLATE = "\n%s has decided to not abandon the game";
    private static final String GAME_ABANDON_DENIED_CURRENT_USER = "\nYou have decided to not abandon the game!";

    private static final String GAME_ENDING_ABANDONED = "\nThe current game has been abandoned by all players!\n" + ScreenUI.GAME_ENDING_RETURN_TO_MAIN;

    public static final String GAME_MY_TURN_ABANDON_PROMPT =
        """
        
        It's your turn now! You have a choice between resuming the game (hit) or abandoning the game(abandon)
        Enter your turn:""";

    @Override
    public String gameMyTurnQuitPrompt() {
        return GAME_MY_TURN_ABANDON_PROMPT;
    }

    @Override
    public String gameQuitCurrentUser() {
        return GAME_ABANDON_CURRENT_USER;
    }

    @Override
    public String gameQuitWaiting() {
        return GAME_ABANDON_WAITING;
    }

    @Override
    public String gameQuitTemplate() {
        return GAME_ABANDON_TEMPLATE;
    }

    @Override
    public String gameQuitDeniedTemplate() {
        return GAME_ABANDON_DENIED_TEMPLATE;
    }

    @Override
    public String gameQuitDeniedCurrentUser() {
        return GAME_ABANDON_DENIED_CURRENT_USER;
    }

    @Override
    public String gameEndingQuit() {
        return GAME_ENDING_ABANDONED;
    }
}
