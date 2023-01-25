package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

// NIO specifics wrapped & hidden
public class ConsoleClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Scanner scanner;
    private final Gson gson;

    private SessionCookie session;

    public ConsoleClient(SocketChannel socketChannel, BufferedReader reader, PrintWriter writer, Scanner scanner) {
        this.socketChannel = socketChannel;
        this.reader = reader;
        this.writer = writer;
        this.scanner = scanner;
        this.gson = new Gson();
        this.session = new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null);
    }

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, "UTF-8"));
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, "UTF-8"), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            var client = new ConsoleClient(socketChannel, reader, writer, scanner);

            //Send initial request to server so that the server can identify which client belongs to which socket!
            //...

            var currentScreenHandler = new ScreenHandler(client, client.session.currentScreen);

            while (true) {
                var currentScreenResponse = currentScreenHandler.executeHandler();

                //Handle call to exit the application
                if (currentScreenResponse.status() == ResponseStatus.EXIT) {
                    break;
                }
                //Handle redirect
                else if (currentScreenResponse.status() == ResponseStatus.REDIRECT ||
                         currentScreenResponse.status() == ResponseStatus.LOGIN ||
                         currentScreenResponse.status() == ResponseStatus.LOGOUT ||
                         currentScreenResponse.status() == ResponseStatus.PENDING_GAME) {

                    var fromScreen = client.session.currentScreen;
                    var toScreen = currentScreenResponse.redirect();

                    if (ScreenInfo.validRedirect(fromScreen, toScreen)) {
                        client.session.currentScreen = toScreen;
                        currentScreenHandler.setHandler(toScreen);
                    }
                    else {
                        throw new RuntimeException("Unexpected error has occurred, the redirect is invalid!");
                    }
                }
                //Handle invalid command
                else if (currentScreenResponse.status() == ResponseStatus.INVALID_COMMAND) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    public ServerResponse guestHomeScreen() throws IOException {
        printlnClean(ScreenUI.GUEST_HOME_PROMPT);

        var serverResponse = sendAndReceive();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse registerScreen() throws IOException {
        printlnClean(ScreenUI.REGISTER_PROMPT);

        var serverResponse = sendAndReceive();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse loginScreen() throws IOException {
        printlnClean(ScreenUI.LOGIN_PROMPT);

        var serverResponse = sendAndReceive();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        if (serverResponse.status() == ResponseStatus.LOGIN) {
            this.session.username = serverResponse.session().username;
        }

        return serverResponse;
    }

    public ServerResponse homeScreen() throws IOException {
        printlnClean(ScreenUI.homePrompt(session.username) +
            ScreenUI.getAvailableCommands(
                CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                CommandInfo.LOG_OUT, CommandInfo.HELP
            ) +
            ScreenUI.enterCommandPrompt());

        var serverResponse = sendAndReceive();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        if (serverResponse.status() == ResponseStatus.LOGOUT) {
            this.session.username = null;
        }
        else if (serverResponse.status() == ResponseStatus.JOINING_GAME) {
            //
//            socketChannel.notifyAll();
        }

        return serverResponse;
    }

    public ServerResponse gameScreen() throws IOException, InterruptedException {
        if (true) {
            printlnClean("\nheyy this is a game. Sick right!");
        }

        //Do something here to make this user sleep until another user joins the game
        var serverResponseRaw = receiveFromServer(socketChannel);

        printlnClean("\nOoooh we found someone haha. Awesome!");

//        var serverResponse = sendAndReceive();

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

//        if (serverResponse.status() == ResponseStatus.LOGOUT) {
//            this.session.username = null;
//        }

        return serverResponse;
    }

    private void printlnClean(String text) {
        System.out.println(ScreenUI.cleanText(text));
    }

    private ServerResponse sendAndReceive() throws IOException {
        var userInput = this.getConsoleInput();

        var request = new ClientRequest(userInput, session);
        var requestJson = gson.toJson(request);

        this.sendToServer(writer, requestJson);
        var serverResponseRaw = receiveFromServer(socketChannel);

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        return serverResponse;
    }

    private String getConsoleInput() {
        return scanner.nextLine();
    }

    private void sendToServer(PrintWriter writer, String input) throws IOException {
//        printlnClean("Sending message <" + input + "> to the server...");
        writer.print(input);
        writer.flush();
    }

    private String receiveFromServer(SocketChannel socketChannel) throws IOException {
        buffer.clear(); // switch to writing mode

        socketChannel.read(buffer);

        buffer.flip(); // switch to reading mode

        if (!buffer.hasRemaining()) {
            return "";
        }

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        String reply = new String(byteArray, "UTF-8");

//        buffer.clear();

        return reply;
    }
}