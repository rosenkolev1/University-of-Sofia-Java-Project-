package bg.sofia.uni.fmi.mjt.battleships.client;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

// NIO specifics wrapped & hidden
public class ConsoleClient {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";

    private static final int BUFFER_SIZE = 512;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    //This string signifies that there is more to read from the socket channel than there is space for in the buffer
    private static String BUFFER_CONTINUES_STRING = null;

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

            try {
                //Send initial request and set initial client state
                //Also set the bufferContinuesString to the initialServerResponse message
                var initialServerResponse = client.sendAndReceiveNoInput();
                client.cookies = initialServerResponse.cookies;
                BUFFER_CONTINUES_STRING = initialServerResponse.message;

                //Now get the first screen info
                client.sendAndReceiveNoInputDefaultHandler();

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

                    //Force client-side error
                    Object a = 1;
                    String b = (String) a;

                    //Handle screen change
                    currentScreenHandler.setHandler(client.cookies.session.currentScreen);
                }
            }
            catch (Exception e) {
                handleClientError(e, client);
            }
        }
        catch (Exception e) {
            handleClientError(e, null);
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

    private ServerResponse receiveSignalDefaultHandler() throws IOException {
        var serverResponseRaw = receiveFromServer(socketChannel);

        var serverResponse = gson.fromJson(serverResponseRaw, ServerResponse.class);

        actOnResponseDefault(serverResponse);

        return serverResponse;
    }

    private ServerResponse sendAndReceiveDefaultHandler() throws IOException {
        var serverResponse = sendAndReceive();

        actOnResponseDefault(serverResponse);

        return serverResponse;
    }

    private ServerResponse sendAndReceiveNoInputDefaultHandler() throws IOException {
        var initialServerResponse = this.sendAndReceiveNoInput();

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

    private ServerResponse sendAndReceiveNoInput() throws IOException {
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

            if (BUFFER_CONTINUES_STRING != null && replyChunk.endsWith(BUFFER_CONTINUES_STRING)) {

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

    private static void handleClientError(Exception e, ConsoleClient client) {

        if (e instanceof IOException) {
            System.out.print("\nThere is a problem with the network communication!\nTry again later!\n");
        }
        else {
            System.out.print("\nAn unknown error has occurred!");
        }

        System.out.println(" Attempting to save the error to a log file...");

        //Save exception to log
        try (
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("logs.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            PrintWriter writer = new PrintWriter(bufferedWriter, true)) {

            if (client != null) {
                var gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

                var clientExceptionInfo = new ClientExceptionInfo(ConsoleClient.SERVER_PORT, ConsoleClient.SERVER_HOST, client.cookies);

                writer
                    .append("\n")
                    .append("Error occurred for client: ")
                    .append(gson.toJson(clientExceptionInfo))
                    .append("\n");
            }

            e.printStackTrace(writer);

            writer.append("\n");

            System.out.println("Successfully saved the error to a log file!\n");
            System.out.println("Contact administrator by providing the logs in \"logs.txt\"");
        }
        catch (Exception ex) {
            System.out.println("Could not save the error to a log file because an unknown error has occurred!");
        }
        finally {
            System.out.println("\nExiting the application!");
        }
    }
}