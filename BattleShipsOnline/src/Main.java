import bg.sofia.uni.fmi.mjt.battleships.client.ConsoleClient;
import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;

public class Main {
    public static void main(String[] args) {

        var user = new User("roskata", "12345678");
        var game = new Game(1, user, user, true);

        System.out.println(game.players.get(0).board.toString() + "\n" + game.players.get(1).board.toString());

        var database = new Database("users.txt");
        var commandExecutor = new CommandExecutor();
        var server = new Server(7777, commandExecutor, database);

        server.start();
    }
}