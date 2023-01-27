import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class Main {
    public static void main(String[] args) {

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

        var database = new Database("users.txt", "games.txt");
        var commandExecutor = new CommandExecutor();
        var server = new Server(7777, commandExecutor, database);

        server.start();
    }
}