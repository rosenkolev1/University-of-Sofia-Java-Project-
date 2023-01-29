package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.*;

public class Game {
    public final long id;
    public String name;
    public int turn;
    public GameStatus status;
    public QuitStatus quitStatus;
    private final boolean randomizedBoards;
    public int playerCapacity;

    public List<Player> players;
    public List<PlayerTurn> turnHistory;


    public Game(long id, String name, int playerCapacity, GameStatus status, boolean randomizedBoards, List<User> users) {
        this.id = id;
        this.name = name;
        this.playerCapacity = playerCapacity;
        this.status = status;
        this.randomizedBoards = randomizedBoards;
        this.turn = 0;
        this.quitStatus = QuitStatus.NONE;

        this.turnHistory = new ArrayList<>();
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

    public boolean gameIsEndedOrDeleted() {
        return this.status == GameStatus.ENDED || this.status == GameStatus.DELETED;
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

    public void hitTile(Player attacker, Player defender, TilePos pos) {
        defender.board.hitTile(pos);
        this.turnHistory.add(new PlayerTurn(attacker.user.username(), defender.board.getTilePosAsString(pos)));
        increaseTurn();

        if (defender.board.allShipsHaveSunk()) {
            defender.status = PlayerStatus.DEAD;

            if (hasEnded()) {
                this.status = GameStatus.ENDED;
            }
        }
    }

    public void hitTile(String attackerName, String defenderName, TilePos pos) {
        var attacker = this.getPlayer(attackerName);
        var defender = this.getPlayer(defenderName);
        hitTile(attacker, defender, pos);
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
