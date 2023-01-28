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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class GameTable extends Table {
    public List<Game> games;

    public GameTable(String tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseGames();
    }

    public Game createGame(String name, int playerCount, GameStatus status, boolean randomizedBoards, List<User> users) {
        var gameId = this.games.stream()
            .map(x -> x.id).max(Long::compare).orElse(0L) + 1;

        return new Game(gameId, name, playerCount, status, randomizedBoards, users);
    }

    public void saveGameFile(Game game) {
        try (var bufferedWriter = Files.newBufferedWriter(tablePath, StandardOpenOption.APPEND)) {
            var gameJson = gson.toJson(game);
            bufferedWriter
                .append(gameJson)
                .append(entrySeparator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public List<Game> games() {
//        return games;
//    }

//    public boolean gameExists(String name, GameStatus... statuses) {
////        return getGame(name, statuses) != null;
//        return this.games
//            .stream().filter(x -> x.name.equals(name) &&
//                x.stat)
//    }

//    public boolean gameExists(String name) {
//        return getGame(name) != null;
//    }

//    public List<Game> pendingGames() {
//        return this.games.stream().filter(x -> x.status == GameStatus.PENDING).toList();
//    }

//    public Game getGame(String name, GameStatus... statuses, boolean excluded) {
//        for (var game : games) {
//            if (game.name.equals(name) &&
//                Arrays.stream(statuses).toList().contains(game.status)) {
//
//                return game;
//            }
//        }
//
//        return null;
//    }
//
//    public Game getGame(String name, GameStatus... statuses) {
//        for (var game : games) {
//            if (game.name.equals(name) &&
//                Arrays.stream(statuses).toList().contains(game.status)) {
//
//                return game;
//            }
//        }
//
//        return null;
//    }
//
//    public Game getGame(String name) {
//        for (var game : games) {
//            if (game.name.equals(name)) {
//                return game;
//            }
//        }
//
//        return null;
//    }

    public void addGame(Game game) {
        this.games.add(game);
    }

    public boolean deleteGame(Game game) {
        return games.remove(game);
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
