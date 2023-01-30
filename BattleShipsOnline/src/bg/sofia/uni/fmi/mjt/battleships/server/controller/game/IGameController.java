package bg.sofia.uni.fmi.mjt.battleships.server.controller.game;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;

public interface IGameController extends IController {
    ServerResponse respond(ClientRequest request);
}
