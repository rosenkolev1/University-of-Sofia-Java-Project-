package bg.sofia.uni.fmi.mjt.battleships.server.controller.home;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;

public interface IHomeController extends IController {
    ServerResponse respond(ClientRequest request);
}
