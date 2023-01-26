package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;

import java.util.*;

public class Game {

    public String name;
    public int playerCount;
    public int turn;
    public GameStatus status;
    public boolean randomizedBoards;
    public List<Player> players;

    public Game(String name, int playerCount, GameStatus status, boolean randomizedBoards, List<User> users) {
        this.name = name;
        this.playerCount = playerCount;
        this.status = status;
        this.randomizedBoards = randomizedBoards;
        this.turn = 0;

        this.players = new ArrayList<>();

        for (var user : users) {
            addPlayer(user);
        }

    }

    public Player getPlayer(String playerName) {
        return this.players.stream().filter(x -> x.user.username().equals(playerName)).findFirst().orElse(null);
    }

    public void addPlayer(User user) {
        var board = new Board(randomizedBoards);

        this.players.add(new Player(user, board));

        if (this.status.equals(GameStatus.PENDING) && this.players.size() == playerCount) {
            this.status = GameStatus.IN_PROGRESS;
        }
    }
}
