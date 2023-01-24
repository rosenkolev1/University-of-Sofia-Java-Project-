import bg.sofia.uni.fmi.mjt.battleships.client.ConsoleClient;
import bg.sofia.uni.fmi.mjt.battleships.server.Server;
import bg.sofia.uni.fmi.mjt.battleships.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;

public class Main {
    public static void main(String[] args) {

        var database = new Database("users.txt");
        var commandExecutor = new CommandExecutor();
        var server = new Server(7777, commandExecutor, database);

        server.start();
    }
}