package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;

import java.util.*;

public class Game {

    public final long id;
    public String name;
    public List<Player> players;
    public int turn;
    private GameStatus status;

    public Game(long id, String name, List<User> users, GameStatus status, boolean randomizedBoards) {
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
        this.status = status;

        for (var user : users) {
            var board = new Board(randomizedBoards);

            this.players.add(new Player(user, board));
        }

        this.turn = 0;
    }
}
