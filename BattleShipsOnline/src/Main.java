import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.ServerOption;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.game.GameController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.GuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.home.HomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.controller.user.UserController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.IUserTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.UserTable;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args)
        throws Exception {

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