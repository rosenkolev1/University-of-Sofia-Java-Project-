package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.QuitStatus;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.Board;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;

public class Player {
    public final User user;

    public Board board;
    public PlayerStatus status;
    public QuitStatus quitStatus;

    public Player(User user, Board board) {
        this.user = user;
        this.board = board;
        this.status = PlayerStatus.ALIVE;
        this.quitStatus = QuitStatus.NONE;
    }
}
