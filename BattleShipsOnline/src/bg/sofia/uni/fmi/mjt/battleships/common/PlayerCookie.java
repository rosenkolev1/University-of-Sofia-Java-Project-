package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerCookie {

    //TODO: rename field "player" to "name"
    public String player;
    public final int myTurn;
    public List<String> moves;
    public int quitStatusCode;

    public PlayerCookie(String player, int myTurn) {
        this(player, myTurn, new ArrayList<>());
    }

    public PlayerCookie(String player, int myTurn, List<String> moves) {
        this(player, myTurn, moves, 0);
    }

    public PlayerCookie(String player, int myTurn, List<String> moves, int quitStatusCode) {
        this.player = player;
        this.myTurn = myTurn;
        this.moves = moves;
        this.quitStatusCode = quitStatusCode;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof PlayerCookie castOther)) {
            return false;
        }

        return this.player.equals(castOther.player) &&
            this.myTurn == castOther.myTurn &&
            this.quitStatusCode == castOther.quitStatusCode &&
            new HashSet<>(this.moves).containsAll(castOther.moves) &&
            new HashSet<>(castOther.moves).containsAll(this.moves);
    }
}
