package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player;

public enum PlayerStatus {
    ALIVE(0),
    DEAD(1);

    private final int statusCode;

    PlayerStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return this.statusCode;
    }
}
