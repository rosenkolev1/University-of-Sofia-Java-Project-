package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerCookie {
    public String name;
    public final int myTurn;
    public List<String> moves;
    public int quitStatusCode;

    public PlayerCookie(String name, int myTurn) {
        this(name, myTurn, new ArrayList<>());
    }

    public PlayerCookie(String name, int myTurn, List<String> moves) {
        this(name, myTurn, moves, 0);
    }

    public PlayerCookie(String name, int myTurn, List<String> moves, int quitStatusCode) {
        this.name = name;
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

        return this.name.equals(castOther.name) &&
            this.myTurn == castOther.myTurn &&
            this.quitStatusCode == castOther.quitStatusCode &&
            new HashSet<>(this.moves).containsAll(castOther.moves) &&
            new HashSet<>(castOther.moves).containsAll(this.moves);
    }
}
