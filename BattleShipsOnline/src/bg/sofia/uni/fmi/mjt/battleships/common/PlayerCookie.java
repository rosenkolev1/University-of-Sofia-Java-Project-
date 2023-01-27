package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerCookie {

    public String player;
    public final int myTurn;
    public List<String> moves;

    public PlayerCookie(String player, int myTurn) {
        this(player, myTurn, new ArrayList<>());
    }

    public PlayerCookie(String player, int myTurn, List<String> moves) {
        this.player = player;
        this.myTurn = myTurn;
        this.moves = moves;
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
            new HashSet<>(this.moves).containsAll(castOther.moves) &&
            new HashSet<>(castOther.moves).containsAll(this.moves);
    }
}
