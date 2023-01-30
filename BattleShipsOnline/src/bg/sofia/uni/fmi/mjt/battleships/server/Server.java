package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.GameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.GuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.HomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
    private static final int BUFFER_SIZE = 512;
    private static final String HOST = "localhost";
    //This string signifies that there is more to read from the socket channel than there is space for in the buffer
    private static final String BUFFER_CONTINUES_STRING = "#c";

    private final int port;
    private final Gson gson;

    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private final Database db;
    private final UserController userController;
    private final HomeController homeController;
    private final GuestHomeController guestHomeController;
    private final GameController gameController;

    public Server(int port, Database db) {
        this.port = port;
        this.db = db;
        this.gson = new Gson();
        this.userController = new UserController(db);
        this.homeController = new HomeController(db);
        this.guestHomeController = new GuestHomeController();
        this.gameController = new GameController(db);
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
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

                            // Get server response
                            var serverResponse = getServerResponse(selector, key, clientRequest);

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
                                            writeClientOutput((SocketChannel) selectionKey.channel(), gson.toJson(signalResponse));
                                        }

                                    }
                                }
                            }

                            //Print the server response for debug purposes
                            System.out.println(serverResponse);

                            var serverResponseJson = gson.toJson(serverResponse);

                            writeClientOutput(clientChannel, serverResponseJson);

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                }
                catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    private ServerResponse getServerResponse(Selector selector, SelectionKey key, ClientRequest clientRequest) {
        ServerResponse serverResponse = null;

        if (clientRequest.cookies().session == null || clientRequest.cookies().session.currentScreen.equals(ScreenInfo.GUEST_HOME_SCREEN)) {
            return guestHomeController.respond(clientRequest);
        }
        if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.LOGIN_SCREEN)) {
            List<SessionCookie> sessions = new ArrayList<>();

            //Get all the currently logged users' session cookies
            for (var selectionKey : selector.keys()) {
                var cookies = (ClientState)selectionKey.attachment();

                if (cookies != null) {
                    sessions.add(cookies.session);
                }
            }

            return userController.loginResponse(sessions, clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.REGISTER_SCREEN)) {
            return userController.registerResponse(clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.HOME_SCREEN)) {
            return homeController.respond(clientRequest);
        }
        else if (clientRequest.cookies().session.currentScreen.equals(ScreenInfo.GAME_SCREEN)) {
            return gameController.respond(clientRequest);
        }

        throw new RuntimeException("An fatal error has occurred! The client's current screen does not exist!");
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
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

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {

        int remainingOutput = output.length();
        int outputIndex = 0;

        while(remainingOutput != 0) {
            boolean bufferHasEnoughSpace = remainingOutput <= BUFFER_SIZE;

            var nextReadCount = bufferHasEnoughSpace ? remainingOutput : (BUFFER_SIZE - BUFFER_CONTINUES_STRING.length());
            remainingOutput -= nextReadCount;

            String chunk = output.substring(outputIndex, outputIndex + nextReadCount);

            outputIndex += nextReadCount;

            if (!bufferHasEnoughSpace) {
                chunk += BUFFER_CONTINUES_STRING;
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
}
