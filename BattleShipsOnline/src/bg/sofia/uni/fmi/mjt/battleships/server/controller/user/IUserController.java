package bg.sofia.uni.fmi.mjt.battleships.server.controller.user;

import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;

import java.util.List;

public interface IUserController extends IController {
    ServerResponse loginRespond(ClientRequest request, List<SessionCookie> sessions);

    ServerResponse registerRespond(ClientRequest request);
}
