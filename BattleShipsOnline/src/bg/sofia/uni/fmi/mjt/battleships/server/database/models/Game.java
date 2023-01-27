package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.*;

import java.util.*;

public class Game {
    public long id;
    public String name;
    public int playerCount;
    public int turn;
    public GameStatus status;
    public boolean randomizedBoards;
    public List<Player> players;

    public Game(long id, String name, int playerCount, GameStatus status, boolean randomizedBoards, List<User> users) {
        this.id = id;
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

    public boolean gameHasEnded() {
        var alivePlayer = this.players.stream().filter(x -> x.status == PlayerStatus.ALIVE).toList();

        return alivePlayer.size() == 1;
    }

    public void hitTile(Player player, TilePos pos) {
        player.board.hitTile(pos);

        if (player.board.allShipsHaveSunk()) {
            player.status = PlayerStatus.DEAD;

            if (gameHasEnded()) {
                this.status = GameStatus.ENDED;
            }
        }
    }

    public void hitTile(String playerName, TilePos pos) {
        var player = this.getPlayer(playerName);
        hitTile(player, pos);
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
