package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.Arrays;

public enum BoardRank {
    A(1),
    B(2),
    C(3),
    D(4),
    E(5),
    F(6),
    G(7),
    H(8),
    I(9),
    J(10);

    public final int rank;

    BoardRank(int rank) {
        this.rank = rank;
    }

    public static BoardRank getBoardRankFrom(String rank) {
        return Arrays.stream(BoardRank.values()).filter(x -> x.toString().equals(rank)).findFirst().get();
    }
}
