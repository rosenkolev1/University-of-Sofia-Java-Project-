package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.ScreenUI;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.SessionCookie;

public class Controller {

    protected ServerResponse invalidCommandResponse(SessionCookie session) {
        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), session);
    }

    protected ServerResponse invalidCommandResponse(String message, SessionCookie session) {
        return new ServerResponse(ResponseStatus.INVALID_COMMAND, null, message, session);
    }
}
