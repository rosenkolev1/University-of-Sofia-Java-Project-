package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.QuitStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.entry.TableEntryInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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

    public TableEntryInfo<Game> getGameEntryInfo(int id) {
        var entries = getTableEntries();
        var matchEntryIndex = -1;
        Game targetEntry = null;

        for (int i = 0; i < entries.size(); i++) {
            var curEntry = entries.get(i);
            var curGame = gson.fromJson(curEntry, Game.class);

            if (curGame.id == id) {
                matchEntryIndex = i;
                targetEntry = curGame;
                break;
            }
        }

        return new TableEntryInfo<>(targetEntry, matchEntryIndex, entries);
    }

    public TableEntryInfo<Game> getGameEntryInfo(Game game) {
        return getGameEntryInfo((int) game.id);
    }

    public void saveGameFile(Game game) {
        var gameJson = gson.toJson(game);

        //Check if game already exists in the file
        var entryInfo = getGameEntryInfo(game);
        entryInfo.entry = game;

        if (entryInfo.entryIndex == -1) {
            try (var bufferedWriter = Files.newBufferedWriter(tablePath, StandardOpenOption.APPEND)) {
                bufferedWriter
                    .append(gameJson)
                    .append(entrySeparator);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            overwriteEntry(entryInfo);
        }
    }

    public void addGame(Game game) {
        this.games.add(game);
    }

    public void deleteGame(Game game) {
        game.status = GameStatus.DELETED;
        game.quitStatus = QuitStatus.NONE;
    }

    public void deleteGameFile(Game game) {
        //Check if game already exists in the file
        var entryInfo = getGameEntryInfo(game);
        entryInfo.entry = null;

        overwriteEntry(entryInfo);
    }

    private void initialiseGames() {
        this.games = new ArrayList<>();

        try (var bufferedReader = Files.newBufferedReader(tablePath)) {
            var entries = getTableEntries();

            for (var entryLine : entries) {
                var game = gson.fromJson(entryLine, Game.class);

                this.games.add(game);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
