package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.ScreenUI;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;

public class Controller {

    protected ServerResponse invalidCommandResponse() {
        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND));
    }

    protected ServerResponse invalidCommandResponse(String message) {
        return new ServerResponse(ResponseStatus.INVALID_COMMAND, null, message);
    }
}
