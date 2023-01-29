package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.*;

public class Game {
    //TODO: Add a list containing the history of all the moves maybe!

    public final long id;
    public String name;
    public int playerCapacity;
    public int turn;
    public GameStatus status;
    public QuitStatus quitStatus;
    public List<Player> players;
    private boolean randomizedBoards;

    public Game(long id, String name, int playerCapacity, GameStatus status, boolean randomizedBoards, List<User> users) {
        this.id = id;
        this.name = name;
        this.playerCapacity = playerCapacity;
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

    private void increaseTurn() {
        this.turn++;

        if (this.turn == this.players.size()) {
            this.turn = 0;
        }
    }

    public void quitGame(Player player, QuitStatus quitStatus) {
        player.quitStatus = quitStatus;
        increaseTurn();

        if (hasBeenQuit(quitStatus)) {
            this.quitStatus = quitStatus;
        }
    }

    private void changeGameStateWhenResumed() {
        this.status = GameStatus.IN_PROGRESS;
        this.quitStatus = QuitStatus.NONE;
    }

    public boolean playerBelongsToSavedGame(String playerName) {
        return this.players.stream().anyMatch(x -> x.user.username().equals(playerName) && x.quitStatus == QuitStatus.SAVE_AND_QUIT);
    }

    public boolean savedGameIsResumed() {
        for (var gamePlayer : players) {
            if (gamePlayer.quitStatus != QuitStatus.NONE) {
                return false;
            }
        }

        return true;
    }

    public void resumeGame(Player player) {
        player.quitStatus = QuitStatus.NONE;
        this.status = GameStatus.PENDING;

        //Check if all players are back in the game and if so, resume the game
        if (savedGameIsResumed()) {
            changeGameStateWhenResumed();
        }
    }

    public void resumeGame() {
        for (var player : players) {
            player.quitStatus = QuitStatus.NONE;
        }

        changeGameStateWhenResumed();
    }

    private boolean hasEnded() {
        var alivePlayer = this.players.stream().filter(x -> x.status == PlayerStatus.ALIVE).toList();

        return alivePlayer.size() == 1;
    }

    public void hitTile(Player player, TilePos pos) {
        player.board.hitTile(pos);
        increaseTurn();

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

        if (this.status.equals(GameStatus.PENDING) && this.players.size() == playerCapacity) {
            this.status = GameStatus.IN_PROGRESS;
        }
    }
}
