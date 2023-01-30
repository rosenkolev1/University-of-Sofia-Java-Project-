package bg.sofia.uni.fmi.mjt.battleships.server.controller.home;

import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;

public interface IHomeController extends IController {
    ServerResponse respond(ClientRequest request);
}
