package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.ResponseStatus;
import bg.sofia.uni.fmi.mjt.battleships.common.ServerResponse;
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

    private ClientState cookies;

    public ConsoleClient(SocketChannel socketChannel, BufferedReader reader, PrintWriter writer, Scanner scanner) {
        this.socketChannel = socketChannel;
        this.reader = reader;
        this.writer = writer;
        this.scanner = scanner;
        this.gson = new Gson();
        this.cookies = new ClientState();
    }

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, "UTF-8"));
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, "UTF-8"), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            var client = new ConsoleClient(socketChannel, reader, writer, scanner);

            //Send initial request and set initial client state
            var initialServerResponse = client.sendAndReceiveInitial();
            client.cookies = initialServerResponse.cookies;
            client.printMessage(initialServerResponse.message);

            var currentScreenHandler = new ScreenHandler(client, client.cookies.session.currentScreen);

            while (true) {
                var currentScreenResponse = currentScreenHandler.executeHandler();

                //Handle screen change
                currentScreenHandler.setHandler(client.cookies.session.currentScreen);

                //Handle call to exit the application
                if (currentScreenResponse.status == ResponseStatus.EXIT) {
                    break;
                }
                //Handle invalid command
                else if (currentScreenResponse.status == ResponseStatus.INVALID_COMMAND) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    public ServerResponse guestHomeScreen() throws IOException {
        var serverResponse = sendAndReceive();

        this.cookies = serverResponse.cookies;

        printMessage(serverResponse.message);

        return serverResponse;
    }

    public ServerResponse registerScreen() throws IOException {
        var serverResponse = sendAndReceive();

        this.cookies = serverResponse.cookies;

        printMessage(serverResponse.message);

        return serverResponse;
    }

    public ServerResponse loginScreen() throws IOException {
        var serverResponse = sendAndReceive();

        this.cookies = serverResponse.cookies;

        printMessage(serverResponse.message);

        return serverResponse;
    }

    public ServerResponse homeScreen() throws IOException {
        var serverResponse = sendAndReceive();

        this.cookies = serverResponse.cookies;

        printMessage(serverResponse.message);

        return serverResponse;
    }

    public ServerResponse gameScreen() throws IOException, InterruptedException {
        ServerResponse serverResponse = null;

        if (cookies.game == null) {
            //Make this user sleep until another user joins the game
            var serverResponseRaw = receiveFromServer(socketChannel);

            serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

            this.cookies = serverResponse.cookies;

            printMessage(serverResponse.message);
//
//            if (serverResponse.status.equals(ResponseStatus.START_GAME)) {
//                this.cookies.game = serverResponse.cookies.game;
//            }
        }
        //Handler when it is this client's turn
        else if (cookies.game.turn == cookies.player.myTurn) {
            serverResponse = sendAndReceive();

            this.cookies = serverResponse.cookies;

            printMessage(serverResponse.message);
        }
        //Handler when it is not client's turn
        else {
            //Make this user sleep until another enemy make a move
            var serverResponseRaw = receiveFromServer(socketChannel);

            serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

            this.cookies.session = serverResponse.cookies.session;

            //In this case, the game has been abandoned by all players
            if (serverResponse.status == ResponseStatus.ABANDON_GAME) {
                this.cookies.game = null;
                this.cookies.player = null;
            }
            //In this case, the abandon has been denied and the game is resuming
            else if (serverResponse.status == ResponseStatus.RESUME_GAME) {
//                this.cookies.game.turn = serverResponse.cookies.game.turn;
//                this.cookies.game = serverResponse.cookies.game;
                this.cookies = serverResponse.cookies;
            }
            //In this case the game has not ended
            else if (serverResponse.cookies.game != null) {
//                this.cookies.game.turn = serverResponse.cookies.game.turn;
//                this.cookies.game.playersInfo = serverResponse.cookies.game.playersInfo;
//                this.cookies.game.abandonPlayer
//                this.cookies.game = serverResponse.cookies.game;
                this.cookies = serverResponse.cookies;
            }
            //In this case, the games has ended and our client has lost
            else {
                this.cookies.game = null;
                this.cookies.player = null;
            }

            printMessage(serverResponse.message);
        }

        return serverResponse;
    }

    private void printMessage(String message) {
        if (message != null) {
            System.out.println(message);
        }
    }

    private ServerResponse sendAndReceiveInitial() throws IOException {
        var request = new ClientRequest(null, cookies);
        var requestJson = gson.toJson(request);

        this.sendToServer(writer, requestJson);
        var serverResponseRaw = receiveFromServer(socketChannel);

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        return serverResponse;
    }

    private ServerResponse sendAndReceive() throws IOException {
        var userInput = this.getConsoleInput();

        var request = new ClientRequest(userInput, cookies);
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