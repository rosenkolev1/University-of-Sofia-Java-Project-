import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;

public class Main {
    public static void main(String[] args) {

//        var database = new Database("users.txt", "games.txt", "|~^^~|", " ");
        var database = new Database("users.txt", "games.txt");
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

        var server = new Server(7777, database);

        server.start();
    }
}