package bg.sofia.uni.fmi.mjt.battleships.common.cookie;

import bg.sofia.uni.fmi.mjt.battleships.common.list.ListUtil;

import java.util.*;

public class PlayerCookie {
    public String name;
    public final int myTurn;
    public List<String> moves;
    public int playerStatusCode;
    public int quitStatusCode;

    public PlayerCookie(String name, int myTurn) {
        this(name, myTurn, new ArrayList<>());
    }

    public PlayerCookie(String name, int myTurn, List<String> moves) {
        this(name, myTurn, moves, 0);
    }

    public PlayerCookie(String name, int myTurn, List<String> moves, int playerStatusCode) {
        this(name, myTurn, moves, playerStatusCode, 0);
    }

    public PlayerCookie(String name, int myTurn, List<String> moves, int playerStatusCode, int quitStatusCode) {
        this.name = name;
        this.myTurn = myTurn;
        this.moves = moves;
        this.playerStatusCode = playerStatusCode;
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

        return Objects.equals(this.name, castOther.name) &&
            this.myTurn == castOther.myTurn &&
            this.playerStatusCode == castOther.playerStatusCode &&
            this.quitStatusCode == castOther.quitStatusCode &&
            ListUtil.haveSameElements(this.moves, castOther.moves);
    }
}
