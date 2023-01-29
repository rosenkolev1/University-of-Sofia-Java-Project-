package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.tile;

public enum TileStatus {
    EMPTY("_"),
    SHIP("*"),
    HIT_SHIP("X"),
    HIT_EMPTY("O"),
    FOG("~");

    private final String tile;

    TileStatus(String tile) {
        this.tile = tile;
    }

    @Override
    public String toString() {
        return this.tile;
    }
}
