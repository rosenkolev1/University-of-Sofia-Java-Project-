package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;

public class Controller {

    protected ServerResponse invalidCommandResponse(ClientRequest request) {
        return this.invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), request);
    }

    protected ServerResponse invalidCommandResponse(String message, ClientRequest request) {
        return new ServerResponse(ResponseStatus.INVALID_COMMAND, null, message, request.session(), request.game());
    }

//    protected ServerResponse invalidCommandResponse(GameCookie gameCookie) {
//        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), null, gameCookie);
//    }
//
//    protected ServerResponse invalidCommandResponse(SessionCookie session) {
//        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), session, null);
//    }
//
//    protected ServerResponse invalidCommandResponse(SessionCookie session, GameCookie game) {
//        return invalidCommandResponse(ScreenUI.invalidWithHelp(ScreenUI.INVALID_COMMAND), session, game);
//    }
//
//    protected ServerResponse invalidCommandResponse(String message, SessionCookie session, GameCookie game) {
//        return new ServerResponse(ResponseStatus.INVALID_COMMAND, null, message, session, game);
//    }
}
