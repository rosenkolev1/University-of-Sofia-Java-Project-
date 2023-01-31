package bg.sofia.uni.fmi.mjt.battleships.server;

import bg.sofia.uni.fmi.mjt.battleships.server.controller.Controller;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.IController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.game.IGameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.IGuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.home.IHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.IUserController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import com.google.gson.Gson;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServerOption {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 7777;

    private static final int DEFAULT_BUFFER_SIZE = 512;
    private static final String DEFAULT_CHANNEL_NOT_EMPTY_STRING = "#c#";

    private static final Gson DEFAULT_JSON_PROVIDER = new Gson();

    private int bufferSize;
    private String host;

    //This string signifies that there is more to read from the socket channel than there is space for in the buffer
    private String channelNotEmptyString;

    private int port;
    private Gson gson;

    private IDatabase db;

    private List<IController> controllerList;

    public ServerOption() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;

        this.bufferSize = DEFAULT_BUFFER_SIZE;
        this.channelNotEmptyString = DEFAULT_CHANNEL_NOT_EMPTY_STRING;

        this.gson = DEFAULT_JSON_PROVIDER;

        this.controllerList = new ArrayList<>();
    }

    public ServerOption setHost(String host) {
        this.host = host;
        return this;
    }

    public ServerOption setPort(int port) {
        this.port = port;
        return this;
    }

    public ServerOption setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public ServerOption setChannelNotEmptyString(String notEmptyString) {
        this.channelNotEmptyString = notEmptyString;
        return this;
    }

    public ServerOption setJsonProvider(Gson gson) {
        this.gson = gson;
        return this;
    }

    public ServerOption setDatabase(IDatabase db) {
        this.db = db;
        return this;
    }

    public <T extends Controller> ServerOption addController(Class<T> controllerClass)
        throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        var classIsAbstract = Modifier.isAbstract(controllerClass.getModifiers());

        var controllerConstructor = controllerClass.getDeclaredConstructor(IDatabase.class);

        if (!classIsAbstract) {
            var controllerInstance = controllerConstructor.newInstance(db);

            this.controllerList.add(controllerInstance);

            return this;
        }

        throw new RuntimeException("Cannot pass an abstract controller class!");
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public int bufferSize() {
        return this.bufferSize;
    }

    public String channelNotEmptyString() {
        return this.channelNotEmptyString;
    }

    public Gson jsonProvider() {
        return this.gson;
    }

    public IDatabase database() {
        return this.db;
    }

    public List<IController> controllers() {
        return this.controllerList;
    }
}
