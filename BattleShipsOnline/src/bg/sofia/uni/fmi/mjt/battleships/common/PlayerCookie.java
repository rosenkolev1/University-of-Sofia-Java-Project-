package bg.sofia.uni.fmi.mjt.battleships.common;

public class PlayerCookie {

    public String player;
    public String move;

    public PlayerCookie(String player) {
        this(player, null);
    }

    public PlayerCookie(String player, String move) {
        this.player = player;
        this.move = move;
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
            this.move.equals(castOther.move);
    }
}
