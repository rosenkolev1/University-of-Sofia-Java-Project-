package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.HashSet;
import java.util.List;

public class GameCookie {
    public final String name;
    public final int myTurn;

    public int turn;
    public List<PlayerCookie> playersInfo;

    public GameCookie(String name, int myTurn, int turn, List<PlayerCookie> opponentsInfo) {
        this.name = name;
        this.myTurn = myTurn;
        this.turn = turn;
        this.playersInfo = opponentsInfo;
    }

    public void nextTurn() {
        this.turn += 1;

        if (this.turn >= playersInfo.size()) {
            this.turn = 0;
        }
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof GameCookie castOther)) {
            return false;
        }

        return this.name.equals(castOther.name) &&
            this.turn == castOther.turn && this.myTurn == castOther.myTurn &&
            new HashSet<>(this.playersInfo).containsAll(castOther.playersInfo) &&
            new HashSet<>(castOther.playersInfo).containsAll(this.playersInfo);
    }
}
