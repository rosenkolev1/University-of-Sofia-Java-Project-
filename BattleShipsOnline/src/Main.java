import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.ServerOption;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.game.GameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.GuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.home.HomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IUserTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.UserTable;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args)
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

//        var database = new Database("users.txt", "games.txt", "|~^^~|", " ");

//
//        var game = new Game(3, "kur", 2, null, false, List.of());
//
//        var entryInfo = database.gameTable.getGameEntryInfo(game);
//        var DEBUG = 1;

//        var user = new User("roskata", "12345678");
//        var game = new Game("someGame", 2, GameStatus.IN_PROGRESS, true, List.of(user, user));
//
//        var gson = new Gson();
//        var something = gson.toJson(game);
//        System.out.println(something);
//
//        var gameFromJson = gson.fromJson(something, Game.class);
//
//        System.out.println(game.players.get(0).board.toString() + "\n" + game.players.get(1).board.toString());
//
//        System.out.println(game.players.get(0).board.toString().equals(gameFromJson.players.get(0).board.toString() ));
//        System.out.println(game.players.get(1).board.toString().equals(gameFromJson.players.get(1).board.toString() ));
//
//        Assertions.assertIterableEquals(game.players.get(0).board.ships(), gameFromJson.players.get(0).board.ships());
//        Assertions.assertIterableEquals(game.players.get(1).board.ships(), gameFromJson.players.get(1).board.ships());

//    private static final String defaultEntrySeparator = "\n";
//    private static final String defaultFieldSeparator = " ";

        IUserTable userTable = new UserTable("users.txt", "\n", " ");
        IGameTable gameTable = new GameTable("games.txt", "\n", " ");
        var database = new Database(userTable, gameTable);

        var serverOptions = new ServerOption()
            .setHost("localhost")
            .setPort(7777)
            .setBufferSize(512)
            .setChannelNotEmptyString("#c#")
            .setJsonProvider(new Gson())
            .setDatabase(database)
            .addController(GuestHomeController.class)
            .addController(UserController.class)
            .addController(HomeController.class)
            .addController(GameController.class);


        var server = new Server(serverOptions);

        server.start();
    }
}