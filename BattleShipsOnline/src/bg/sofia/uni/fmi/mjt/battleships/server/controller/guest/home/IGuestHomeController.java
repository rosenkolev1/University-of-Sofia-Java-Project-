package bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home;

import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;

public interface IGuestHomeController extends IController {
    ServerResponse respond(ClientRequest request);
    ServerResponse initialResponse(ClientRequest request, String channelNotEmptyString);
}
