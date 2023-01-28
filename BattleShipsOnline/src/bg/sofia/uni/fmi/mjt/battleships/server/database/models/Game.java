package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.*;

public class Game {
    private boolean randomizedBoards;

    public long id;
    public String name;
    public int playerCount;
    public int turn;
    public GameStatus status;
    public QuitStatus quitStatus;
    public List<Player> players;

    public Game(long id, String name, int playerCount, GameStatus status, boolean randomizedBoards, List<User> users) {
        this.id = id;
        this.name = name;
        this.playerCount = playerCount;
        this.status = status;
        this.randomizedBoards = randomizedBoards;
        this.turn = 0;
        this.quitStatus = QuitStatus.NONE;

        this.players = new ArrayList<>();

        for (var user : users) {
            addPlayer(user);
        }

    }

    private boolean hasBeenQuit(QuitStatus quitStatus) {
        for (var player : players) {
            if (player.quitStatus != quitStatus) {
                return false;
            }
        }

        return true;
    }

    public void quitGame(Player player, QuitStatus quitStatus) {
        player.quitStatus = quitStatus;

        if (hasBeenQuit(quitStatus)) {
            this.quitStatus = quitStatus;
        }
    }

    public void resumeGame() {
        for (var player : players) {
            player.quitStatus = QuitStatus.NONE;
        }
    }

    private boolean hasEnded() {
        var alivePlayer = this.players.stream().filter(x -> x.status == PlayerStatus.ALIVE).toList();

        return alivePlayer.size() == 1;
    }

    public void hitTile(Player player, TilePos pos) {
        player.board.hitTile(pos);

        if (player.board.allShipsHaveSunk()) {
            player.status = PlayerStatus.DEAD;

            if (hasEnded()) {
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
