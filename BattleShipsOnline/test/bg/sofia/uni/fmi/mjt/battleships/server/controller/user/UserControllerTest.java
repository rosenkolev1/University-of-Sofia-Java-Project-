package bg.sofia.uni.fmi.mjt.battleships.server.controller.user;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.response.ServerResponse;
import bg.sofia.uni.fmi.mjt.battleships.common.screen.ScreenInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandInfo;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.ControllerTest;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.UserTable;
import bg.sofia.uni.fmi.mjt.battleships.server.ui.ScreenUI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends ControllerTest {
    public static UserController controller;

    @Mock
    public static UserTable userTable;

    @InjectMocks
    public static Database db;

    @Test
    void testLoginInvalidArgumentsCount() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("asd asd ads", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        );
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPromptDefault(ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testLoginUserDoesNotExist() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("rosen 123456789", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        );
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_USER_DOES_NOT_EXIST, ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testLoginUserHasAlreadyLoggedIn() {
        when(userTable.userExists(any()))
            .thenReturn(true);

        controller = new UserController(db);

        var clientRequest = new ClientRequest("rosen 123456789", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of(
            new SessionCookie(ScreenInfo.HOME_SCREEN, "rosen")
        ));

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        );
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_USER_ALREADY_LOGGED_IN, ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testLoginUserSuccess() {
        when(userTable.userExists(any()))
            .thenReturn(true);

        controller = new UserController(db);

        var clientRequest = new ClientRequest("rosen 123456789", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.HOME_SCREEN, "rosen"),null,null
        );
        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.SUCCESSFUL_LOGIN + ScreenUI.getScreenPrompt(ScreenInfo.HOME_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testLoginUserBack() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("back", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null),null,null
        );

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
    void testLoginUserHelp() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("help", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.helpScreenPrompt(clientRequest,
                CommandInfo.LOGIN_CREDENTIALS, CommandInfo.BACK))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testLoginUserInvalidCommand() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("asd", new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        ));

        var serverResponse = controller.loginRespond(clientRequest, List.of());

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.LOGIN_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPromptDefault(ScreenInfo.LOGIN_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterUserInvalidArgumentsCount() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("asd asd asd", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPromptDefault(ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterUserPasswordTooShort() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("roskata haha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_PASSWORD_LENGTH, ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterUserIllegalSymbolsInUsername() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("ros^^ hahahahaha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_USERNAME_FORBIDDEN_CHARS, ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterUserIllegalSymbolsInPassword() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("roskata ^^hahahahaha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_PASSWORD_FORBIDDEN_CHARS, ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterUsernameAlreadyExists() {
        when(userTable.userExistWithName(any()))
            .thenReturn(true);


        controller = new UserController(db);

        var clientRequest = new ClientRequest("roskata hahahahaha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_USERNAME_TAKEN, ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterPasswordAlreadyExists() {
        when(userTable.userExistWithPassword(any()))
            .thenReturn(true);

        controller = new UserController(db);

        var clientRequest = new ClientRequest("roskata hahahahaha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPrompt(ScreenUI.INVALID_PASSWORD_TAKEN, ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterSuccess() {
        userTable = Mockito.spy(new UserTable(usersPath, "\n", " "));

        db = Mockito.spy(new Database(userTable, null));

        controller = new UserController(db);

        var clientRequest = new ClientRequest("roskata hahahahaha", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.SUCCESSFUL_REGISTRATION + ScreenUI.getScreenPrompt(ScreenInfo.GUEST_HOME_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");

        Assertions.assertNotNull(userTable.getUser("roskata"),
            "The user was not actually added to the database!");
    }

    @Test
    void testRegisterBack() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("back", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null),null,null
        );

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
    void testRegisterHelp() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("help", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.helpScreenPrompt(clientRequest,
                CommandInfo.REGISTER_CREDENTIALS, CommandInfo.BACK, CommandInfo.HELP))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.OK)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }

    @Test
    void testRegisterInvalidCommand() {
        controller = new UserController(db);

        var clientRequest = new ClientRequest("asdasdasd", new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        ));

        var serverResponse = controller.registerRespond(clientRequest);

        var expectedCookies = new ClientState(
            new SessionCookie(ScreenInfo.REGISTER_SCREEN, null),null,null
        );

        var expectedResponse = ServerResponse
            .builder()
            .setMessage(ScreenUI.invalidScreenPromptDefault(ScreenInfo.REGISTER_SCREEN, expectedCookies))
            .setCookies(expectedCookies)
            .setSignals(null)
            .setStatus(ResponseStatus.INVALID_COMMAND)
            .build();

        Assertions.assertEquals(expectedResponse, serverResponse,
            "The server response is incorrect!");
    }
}
