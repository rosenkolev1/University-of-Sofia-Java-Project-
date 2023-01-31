package bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.screen.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.ControllerTest;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.UserTable;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuestHomeControllerTest extends ControllerTest {
    public static GuestHomeController controller;

    @BeforeAll
    static void initializeController()
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        var userTable = new UserTable(Path.of("test", "test_users.txt"), "\n", " ");
        var gameTable = new GameTable(Path.of("test", "test_games.txt"), "\n", " ");
        var db = new Database(userTable, gameTable);

        controller = new GuestHomeController(db);
    }

    @Test
    void testInitialRespond() {
        var clientRequest = new ClientRequest(null, new ClientState());
        String channelNotEmptyString = "#c#";

        var serverResponse = controller.initialResponse(clientRequest, channelNotEmptyString);

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(channelNotEmptyString)
            .setCookies(new ClientState(new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null))
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRespondNullInputSendsYouToHomeScreen() {
        var clientRequest = new ClientRequest(null, new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.getScreenPrompt(ScreenInfo.GUEST_HOME_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRespondExitInput() {
        var clientRequest = new ClientRequest("exit", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.EXIT_SUCCESS)
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.EXIT)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRespondLoginInput() {
        //Test for "login"
        var clientRequest = new ClientRequest("login", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.LOGIN_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.getScreenPrompt(ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect for the long version of login!");

        //Test for the shorthand "l"
        clientRequest = new ClientRequest("l", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        serverResponse = controller.respond(clientRequest);

        expectedCookies = new ClientState(new SessionCookie(ScreenInfo.LOGIN_SCREEN, null), null, null);
        expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.getScreenPrompt(ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect for the shorthand version of login!");
    }

    @Test
    void testRespondRegisterInput() {
        //Test for the shorthand "l"
        var clientRequest = new ClientRequest("register", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.REGISTER_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.getScreenPrompt(ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect for the shorthand version of register!");

        //Test for the shorthand "l"
        clientRequest = new ClientRequest("r", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        serverResponse = controller.respond(clientRequest);

        expectedCookies = new ClientState(new SessionCookie(ScreenInfo.REGISTER_SCREEN, null), null, null);
        expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.getScreenPrompt(ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect for the shorthand version of register!");
    }

    @Test
    void testRespondHelpInput() {
        //Test for the shorthand "l"
        var clientRequest = new ClientRequest("help", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.helpScreenPrompt(clientRequest,
                CommandInfo.REGISTER, CommandInfo.REGISTER_SHORTHAND,
                CommandInfo.LOGIN, CommandInfo.LOGIN_SHORTHAND,
                CommandInfo.EXIT, CommandInfo.HELP))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRespondInvalidInput() {
        //Test for the shorthand "l"
        var clientRequest = new ClientRequest("as", new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null
        ));

        var serverResponse = controller.respond(clientRequest);

        var expectedCookies = new ClientState(new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null), null, null);
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPromptDefault(ScreenInfo.GUEST_HOME_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }
}
