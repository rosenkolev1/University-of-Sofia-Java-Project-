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
    private final PrintWriter writer;
    private final Scanner scanner;
    private final Gson gson;

    private ClientState cookies;

    public ConsoleClient(SocketChannel socketChannel, PrintWriter writer, Scanner scanner) {
        this.socketChannel = socketChannel;
        this.writer = writer;
        this.scanner = scanner;
        this.gson = new Gson();
        this.cookies = new ClientState();
    }

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             PrintWriter writer = new PrintWriter(Channels.newWriter(socketChannel, "UTF-8"), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            var client = new ConsoleClient(socketChannel, writer, scanner);

            //Send initial request and set initial client state
            var initialServerResponse = client.sendAndReceiveInitialHandler();

            var currentScreenHandler = new ScreenHandler(client, client.cookies.session.currentScreen);

            while (true) {
                var currentScreenResponse = currentScreenHandler.executeHandler();

                //Handle call to exit the application
                if (currentScreenResponse.status == ResponseStatus.EXIT) {
                    break;
                }
                //Handle error
                else if (currentScreenResponse.status == ResponseStatus.INVALID_COMMAND) {

                }

                //Handle screen change
                currentScreenHandler.setHandler(client.cookies.session.currentScreen);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    public ServerResponse guestHomeScreen() throws IOException {
        return sendAndReceiveDefaultHandler();
    }

    public ServerResponse registerScreen() throws IOException {
        return sendAndReceiveDefaultHandler();
    }

    public ServerResponse loginScreen() throws IOException {
        return sendAndReceiveDefaultHandler();
    }

    public ServerResponse homeScreen() throws IOException {
        return sendAndReceiveDefaultHandler();
    }

    public ServerResponse gameScreen() throws IOException {
        //Handler when it is this client's turn
        if (cookies.game != null && cookies.game.turn == cookies.player.myTurn) {
            return sendAndReceiveDefaultHandler();
        }

        //Handler when it is not this client's turn (or the game has not even started at all)
        return receiveSignalDefaultHandler();
    }

    public ServerResponse receiveSignalDefaultHandler() throws IOException {
        var serverResponseRaw = receiveFromServer(socketChannel);

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        actOnResponseDefault(serverResponse);

        return serverResponse;
    }

    public ServerResponse sendAndReceiveDefaultHandler() throws IOException {
        var serverResponse = sendAndReceive();

        actOnResponseDefault(serverResponse);

        return serverResponse;
    }

    private ServerResponse sendAndReceiveInitialHandler() throws IOException {
        var initialServerResponse = this.sendAndReceiveInitial();

        actOnResponseDefault(initialServerResponse);

        return initialServerResponse;
    }

    private void printMessage(String message) {
        if (message != null) {
            System.out.println(message);
        }
    }

    private void actOnResponseDefault(ServerResponse response) {
        this.cookies = response.cookies;

        this.printMessage(response.message);
    }

    private ServerResponse sendRequestAndReceive(ClientRequest request) throws IOException {
        var requestJson = gson.toJson(request);

        this.sendToServer(writer, requestJson);
        var serverResponseRaw = receiveFromServer(socketChannel);

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        return serverResponse;
    }

    private ServerResponse sendAndReceiveInitial() throws IOException {
        var request = new ClientRequest(null, cookies);

        return sendRequestAndReceive(request);
    }

    private ServerResponse sendAndReceive() throws IOException {
        var userInput = this.getConsoleInput();

        var request = new ClientRequest(userInput, cookies);

        return sendRequestAndReceive(request);
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