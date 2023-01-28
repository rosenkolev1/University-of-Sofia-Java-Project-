package bg.sofia.uni.fmi.mjt.battleships.common;

import java.util.HashSet;
import java.util.List;

public class GameCookie {
    public final String name;
    public int turn;
    public List<PlayerCookie> playersInfo;
    //The player that originally started the quit attempt
    public PlayerCookie quitPlayer;

    public GameCookie(GameCookie cookie) {
        this.name = cookie.name;
        this.turn = cookie.turn;
        this.playersInfo = cookie.playersInfo;
        this.quitPlayer = cookie.quitPlayer;
    }

    public GameCookie(String name, int turn, List<PlayerCookie> opponentsInfo) {
        this.name = name;
        this.turn = turn;
        this.playersInfo = opponentsInfo;
        this.quitPlayer = null;
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
            this.turn == castOther.turn &&
            new HashSet<>(this.playersInfo).containsAll(castOther.playersInfo) &&
            new HashSet<>(castOther.playersInfo).containsAll(this.playersInfo) &&
            this.quitPlayer.equals(castOther.quitPlayer);
    }
}
