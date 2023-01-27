package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GameTable extends Table {

    private List<Game> games;

    public GameTable(Path tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseGames();
    }

    public GameTable(String tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseGames();
    }

    public List<Game> games() {
        return games;
    }

    public boolean gameExists(String name) {
        return getGame(name) != null;
    }

    public List<Game> pendingGames() {
        return this.games.stream().filter(x -> x.status == GameStatus.PENDING).toList();
    }

    public Game getGame(String name) {
        for (var game : games) {
            if (game.name.equals(name)) {
                return game;
            }
        }

        return null;
    }

    public void addGame(Game game) {
        try (var bufferedWriter = Files.newBufferedWriter(tablePath, StandardOpenOption.APPEND)) {
            this.games.add(game);

//            var gameJson = gson.toJson(game);
//            bufferedWriter.append(gameJson + entrySeparator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialiseGames() {
        this.games = new ArrayList<>();

        try (var bufferedReader = Files.newBufferedReader(tablePath)) {
            while(true) {
                var line = bufferedReader.readLine();

                if (line == null) {
                    break;
                }

                var game = gson.fromJson(line, Game.class);

                this.games.add(game);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
