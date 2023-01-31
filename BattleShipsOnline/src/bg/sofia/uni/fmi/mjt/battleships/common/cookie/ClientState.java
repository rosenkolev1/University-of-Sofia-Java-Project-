package bg.sofia.uni.fmi.mjt.battleships.common.cookie;

import java.util.Objects;

public class ClientState {
    public SessionCookie session;
    public PlayerCookie player;
    public GameCookie game;

    public ClientState() {
        this(null, null, null);
    }

    public ClientState(SessionCookie session, PlayerCookie player, GameCookie game) {
        this.session = session;
        this.player = player;
        this.game = game;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ClientState castOther)) {
            return false;
        }

        return Objects.equals(this.session, castOther.session) &&
            Objects.equals(this.player, castOther.player) &&
            Objects.equals(this.game, castOther.game);
    }
}
