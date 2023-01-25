package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.List;

public class Player {
    public final User user;

    public Board board;

    public Player(User user, Board board) {
        this.user = user;
        this.board = board;
    }
}
