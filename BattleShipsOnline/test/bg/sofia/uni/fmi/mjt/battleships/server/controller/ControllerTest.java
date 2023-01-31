package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.server.controller.guest.home.GuestHomeController;
import bg.sofia.uni.fmi.mjt.battleships.server.database.Database;
import bg.sofia.uni.fmi.mjt.battleships.server.database.IDatabase;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.UserTable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ControllerTest {

    protected static Path usersPath = Path.of("test", "test_users.txt");
    protected static Path gamesPath = Path.of("test", "test_games.txt");

    @BeforeAll
    protected static void createDatabaseFiles() throws IOException {
        Files.createFile(usersPath);
        Files.createFile(gamesPath);
    }

    @AfterAll
    protected static void cleanDatabaseFiles() throws IOException {
        Files.delete(Path.of("test", "test_users.txt"));
        Files.delete(Path.of("test", "test_games.txt"));
    }
}
