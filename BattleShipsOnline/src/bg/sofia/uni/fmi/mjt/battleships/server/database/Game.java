package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;

import java.util.*;

public class Game {

    private final long id;

    public List<Player> players;

    private int turn;

    public Game(long id, User user1, User user2, boolean randomizedBoards) {
        this.id = id;

        var board1 = new Board(randomizedBoards);
        var board2 = new Board(randomizedBoards);

        this.players = new ArrayList<>();

        this.players.add(new Player(user1, board1));
        this.players.add(new Player(user2, board2));

        this.turn = 0;
    }
}
