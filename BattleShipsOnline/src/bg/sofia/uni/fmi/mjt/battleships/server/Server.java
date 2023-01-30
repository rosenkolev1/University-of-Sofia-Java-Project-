package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.client.ConsoleClient;
import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.game.GameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.game.IGameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.GuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.IGuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.home.HomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.home.IHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.IUserController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
    private boolean serverErrorCaught = false;

    private final int bufferSize;
    private final String host;
    //This string signifies that there is more to read from the socket channel than there is space for in the buffer
    private final String channelNotEmptyString;

    private final int port;
    private final Gson gson;

    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private final IDatabase db;
    private final IGuestHomeController guestHomeController;
    private final IUserController userController;
    private final IHomeController homeController;
    private final IGameController gameController;

    public Server (ServerOption options) {
        this.host = options.host();
        this.port = options.port();

        this.bufferSize = options.bufferSize();
        this.channelNotEmptyString = options.channelNotEmptyString();

        this.gson = options.jsonProvider();

        this.db = options.database();

        IGuestHomeController tempGuestHomeController = null;
        IUserController tempUserController = null;
        IHomeController tempHomeController = null;
        IGameController tempGameController = null;

        //TODO: Change to be more generic
        for (var controller : options.controllers()) {
            if (controller instanceof IGuestHomeController castController) {
                tempGuestHomeController = castController;
            }
            else if (controller instanceof IUserController castController) {
                tempUserController = castController;
            }
            else if (controller instanceof IHomeController castController) {
                tempHomeController = castController;
            }
            else if (controller instanceof IGameController castController) {
                tempGameController = castController;
            }
        }

        this.guestHomeController = tempGuestHomeController;
        this.userController = tempUserController;
        this.homeController = tempHomeController;
        this.gameController = tempGameController;
    }

    public void start() throws Exception {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(bufferSize);
            isServerWorking = true;

            System.out.println("The server is working!");

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);

                            if (clientInput == null) {
                                continue;
                            }

                            //Print the client request for debug purposes
                            System.out.println(clientInput);

                            var clientRequest = this.gson.fromJson(clientInput, ClientRequest.class);

                            try {
                                // Get server response
                                var serverResponse = getServerResponse(selector, key, clientRequest);

                                //Force client-side error by uncommenting this
                                Object a = 1;
                                String b = (String) a;

                                //Attach the cookies to the selectionKey so that this client can be identified by other clients
                                key.attach(serverResponse.cookies);

                                //Send any resulting signals to the target clients
                                if (serverResponse.signals != null) {
                                    for (var signalResponse : serverResponse.signals) {
                                        for (var selectionKey : selector.keys()) {
                                            var keySession = (ClientState)selectionKey.attachment();

                                            if (keySession != null &&
                                                keySession.session != null &&
                                                keySession.session.username != null &&
                                                keySession.session.username.equals(signalResponse.cookies.session.username)) {
                                                sendResponseToClient((SocketChannel) selectionKey.channel(), gson.toJson(signalResponse));
                                            }

                                        }
                                    }
                                }
                                //Print the server response for debug purposes
//                                System.out.println(serverResponse);

                                var serverResponseJson = gson.toJson(serverResponse);

                                sendResponseToClient(clientChannel, serverResponseJson);
                            }
                            catch (Exception e) {
                                handleServerError(e,clientRequest);
                            }
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                }
                catch (IOException e) {
                    serverSocketChannel.close();

                    handleServerError(e,null);
                }
            }
        }
        catch (Exception e) {
            handleServerError(e,null);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private ServerResponse getServerResponse(Selector selector, SelectionKey key, ClientRequest clientRequest) {
        if (clientRequest.cookies().session == null) {
            return guestHomeController.initialResponse(clientRequest, channelNotEmptyString);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.GUEST_HOME_SCREEN)) {
            return guestHomeController.respond(clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.LOGIN_SCREEN)) {
            List<SessionCookie> sessions = new ArrayList<>();

            //Get all the currently logged users' cookies
            for (var selectionKey : selector.keys()) {
                var cookies = (ClientState)selectionKey.attachment();

                if (cookies != null) {
                    sessions.add(cookies.session);
                }
            }

            return userController.loginRespond(sessions, clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.REGISTER_SCREEN)) {
            return userController.registerRespond(clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.HOME_SCREEN)) {
            return homeController.respond(clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.GAME_SCREEN)) {
            return gameController.respond(clientRequest);
        }

        throw new RuntimeException("An fatal error has occurred! The client's current screen does not exist!");
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(host, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        var result = new StringBuilder();

        while(true) {
            buffer.clear();

            int readBytes = clientChannel.read(buffer);

            if (readBytes < 0) {
                clientChannel.close();
                return null;
            }

            if (readBytes == 0) {
                break;
            }

            buffer.flip();

            byte[] clientInputBytes = new byte[buffer.remaining()];
            buffer.get(clientInputBytes);

            result.append(new String(clientInputBytes, StandardCharsets.UTF_8));
        }

        return result.toString();
    }

    private void sendResponseToClient(SocketChannel clientChannel, String output) throws IOException {

        int remainingOutput = output.length();
        int outputIndex = 0;

        while(remainingOutput != 0) {
            boolean bufferHasEnoughSpace = remainingOutput <= bufferSize;

            var nextReadCount = bufferHasEnoughSpace ? remainingOutput : (bufferSize - channelNotEmptyString.length());
            remainingOutput -= nextReadCount;

            String chunk = output.substring(outputIndex, outputIndex + nextReadCount);

            outputIndex += nextReadCount;

            if (!bufferHasEnoughSpace) {
                chunk += channelNotEmptyString;
            }

            buffer.clear();
            buffer.put(chunk.getBytes());
            buffer.flip();

            clientChannel.write(buffer);
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);


    }

    private void handleServerError(Exception e, ClientRequest request) throws Exception {
        stop();

        if (!serverErrorCaught) {
            serverErrorCaught = true;

            if (e instanceof IOException) {
                System.out.print("\nCould not start the server!\n");
            }
            else {
                System.out.print("\nAn error has occurred!\n");
            }

            System.out.println("\nAttempting to save the error to a log file...");

            //Save exception to log
            try (
                BufferedWriter bufferedWriter = Files.newBufferedWriter(Path.of("server_errors_logs.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                PrintWriter writer = new PrintWriter(bufferedWriter, true)) {

                if (request != null) {
                    var gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

                    writer
                        .append("\n")
                        .append("Error occurred for client: ")
                        .append(gson.toJson(request))
                        .append("\n");
                }

//                e.printStackTrace();
                e.printStackTrace(writer);

                writer.append("\n");

                System.out.println("Successfully saved the error to a log file!\n");
                System.out.println("Contact administrator by providing the logs in \"logs.txt\"");
            }
            catch (Exception ex) {
                System.out.println("Could not save the error to a log file because an unknown error has occurred!");
            }
            finally {
                throw e;
            }
        }
        else {
            throw e;
        }
    }
}
