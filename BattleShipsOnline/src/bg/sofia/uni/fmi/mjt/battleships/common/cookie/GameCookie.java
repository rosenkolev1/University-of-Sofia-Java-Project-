package bg.sofia.uni.fmi.mjt.battleships.common.cookie;

import bg.sofia.uni.fmi.mjt.battleships.common.list.ListUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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

        return Objects.equals(this.name, castOther.name) &&
            this.turn == castOther.turn &&
            Objects.equals(this.quitPlayer, castOther.quitPlayer) &&
            ListUtil.haveSameElements(this.playersInfo, castOther.playersInfo);
    }
}
