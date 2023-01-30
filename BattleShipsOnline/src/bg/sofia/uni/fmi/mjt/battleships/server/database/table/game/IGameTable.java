package bg.sofia.uni.fmi.mjt.battleships.server.database.table.game;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.Game;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.GameStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;

import java.util.List;

public interface IGameTable {
    List<Game> games();

    Game createGame(String name, int playerCount, GameStatus status, boolean randomizedBoards, List<User> users);

    void saveGameFile(Game game);

    void addGame(Game game);

    void deleteGame(Game game);

    void deleteGameFile(Game game);
}
