package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

// NIO specifics wrapped & hidden
public class ConsoleClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    //This string signifies that there is more to read from the socket channel than there is space for in the buffer
    private static final String BUFFER_CONTINUES_STRING = "#c";
    private static final int BUFFER_SIZE = 512;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Scanner scanner;
    private final Gson gson;

    private SessionCookie session;
    private GameCookie game;

    public ConsoleClient(SocketChannel socketChannel, BufferedReader reader, PrintWriter writer, Scanner scanner) {
        this.socketChannel = socketChannel;
        this.reader = reader;
        this.writer = writer;
        this.scanner = scanner;
        this.gson = new Gson();
        this.session = new SessionCookie(ScreenInfo.GUEST_HOME_SCREEN, null);
        this.game = null;
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

                //Handle screen change
                currentScreenHandler.setHandler(currentScreenResponse.session().currentScreen);

                //Handle call to exit the application
                if (currentScreenResponse.status() == ResponseStatus.EXIT) {
                    break;
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

        this.session = serverResponse.session();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse registerScreen() throws IOException {
        printlnClean(ScreenUI.REGISTER_PROMPT);

        var serverResponse = sendAndReceive();

        this.session = serverResponse.session();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse loginScreen() throws IOException {
        printlnClean(ScreenUI.LOGIN_PROMPT);

        var serverResponse = sendAndReceive();

        this.session = serverResponse.session();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse homeScreen() throws IOException {
        printlnClean(ScreenUI.homePrompt(session.username) +
            ScreenUI.getAvailableCommands(
                CommandInfo.CREATE_GAME_VERBOSE, CommandInfo.LIST_GAMES,
                CommandInfo.JOIN_GAME_VERBOSE, CommandInfo.SAVED_GAMES,
                CommandInfo.LOAD_GAME_VERBOSE, CommandInfo.DELETE_GAME,
                CommandInfo.LOG_OUT, CommandInfo.HELP
            ) +
            ScreenUI.enterCommandPrompt());

        var serverResponse = sendAndReceive();

        this.game = serverResponse.game();
        this.session = serverResponse.session();

        if (serverResponse.message() != null) {
            printlnClean(serverResponse.message());
        }

        return serverResponse;
    }

    public ServerResponse gameScreen() throws IOException, InterruptedException {
        ServerResponse serverResponse = null;

        if (game == null) {
            //Make this user sleep until another user joins the game
            var serverResponseRaw = receiveFromServer(socketChannel);

            serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

            //Set the current screen to the game screen
            // (you can also set the current screen from when you create the signal response in HomeController)
            serverResponse.session().currentScreen = ScreenInfo.GAME_SCREEN;

            this.session = serverResponse.session();

            if (serverResponse.message() != null) {
                printlnClean(serverResponse.message());
            }

            if (serverResponse.status().equals(ResponseStatus.STARTING_GAME)) {
                this.game = serverResponse.game();
            }
        }
        //Handler when it is this client's turn
        else if (game.turn == game.myTurn) {
            printlnClean(ScreenUI.myTurnPrompt(game.playersInfo));

            serverResponse = sendAndReceive();

            this.game = serverResponse.game();
            this.session = serverResponse.session();

            if (serverResponse.message() != null) {
                printlnClean(serverResponse.message());
            }
        }
        //Handler when it is not client's turn
        else {
            printlnClean(ScreenUI.enemyTurnPrompt(game.playersInfo.get(game.turn).player));

            //Make this user sleep until another enemy make a move
            var serverResponseRaw = receiveFromServer(socketChannel);

            serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

            //Set the current screen to the game screen
            // (you can also set the current screen from when you create the signal response in GameController)
            serverResponse.session().currentScreen = ScreenInfo.GAME_SCREEN;
            this.session = serverResponse.session();

            this.game.turn = serverResponse.game().turn;
            this.game.playersInfo = serverResponse.game().playersInfo;

            if (serverResponse.message() != null) {
                printlnClean(serverResponse.message());
            }
        }

        return serverResponse;
    }

    private void printlnClean(String text) {
        System.out.println(ScreenUI.cleanText(text));
    }

    private ServerResponse sendAndReceive() throws IOException {
        var userInput = this.getConsoleInput();

        var request = new ClientRequest(userInput, session, game);
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
        StringBuilder reply = new StringBuilder();

        while(true) {
            buffer.clear(); // switch to writing mode

            socketChannel.read(buffer);

            buffer.flip(); // switch to reading mode

            if (!buffer.hasRemaining()) {
                return "";
            }

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);

            String replyChunk = new String(byteArray, "UTF-8");

            if (replyChunk.endsWith(BUFFER_CONTINUES_STRING)) {

                //Strip away the continues string
                replyChunk = replyChunk.substring(0, replyChunk.lastIndexOf(BUFFER_CONTINUES_STRING));

                reply.append(replyChunk);
            }
            else {
                reply.append(replyChunk);
                break;
            }
        }

        return reply.toString();
    }
}