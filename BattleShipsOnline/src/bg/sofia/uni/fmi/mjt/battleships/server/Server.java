package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.common.*;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandExecutor;
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
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;

    private final int port;
    private final Gson gson;

    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;
    private Database db;

    private UserController userController;
    private HomeController homeController;
    private GuestHomeController guestHomeController;

    public Server(int port, CommandExecutor commandExecutor, Database db) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        this.db = db;
        this.gson = new Gson();
        this.userController = new UserController(db);
        this.homeController = new HomeController();
        this.guestHomeController = new GuestHomeController();
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

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
                            var serverResponse = getServerResponse(clientRequest);

                            //Print the server response for debug purposes
                            System.out.println(serverResponse);

                            var serverResponseJson = gson.toJson(serverResponse);

                            writeClientOutput(clientChannel, serverResponseJson);

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    private ServerResponse getServerResponse(ClientRequest clientRequest) {
        ServerResponse serverResponse = null;

        if (clientRequest.currentScreen().equals(ScreenInfo.LOGIN_SCREEN)) {
            return userController.loginResponse(clientRequest);
        }
        else if (clientRequest.currentScreen().equals(ScreenInfo.REGISTER_SCREEN)) {
            return userController.registerResponse(clientRequest);
        }
        else if (clientRequest.currentScreen().equals(ScreenInfo.HOME_SCREEN)) {
            return homeController.respond(clientRequest);
        }
        else if (clientRequest.currentScreen().equals(ScreenInfo.GUEST_HOME_SCREEN)) {
            return guestHomeController.respond(clientRequest);
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
        buffer.clear();

        int readBytes = clientChannel.read(buffer);

        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}
