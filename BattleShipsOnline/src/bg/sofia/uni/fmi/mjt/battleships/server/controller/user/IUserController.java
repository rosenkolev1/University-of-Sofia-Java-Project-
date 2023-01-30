package bg.sofia.uni.fmi.mjt.battleships.server.controller.user;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;

import java.util.List;

public interface IUserController extends IController {
    ServerResponse loginRespond(List<SessionCookie> sessions, ClientRequest request);

    ServerResponse registerRespond(ClientRequest request);
}
