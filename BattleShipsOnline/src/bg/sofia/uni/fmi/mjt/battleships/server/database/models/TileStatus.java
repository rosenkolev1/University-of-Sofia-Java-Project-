package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

public enum TileStatus {
    EMPTY("_"),
    SHIP("*"),
    HIT_SHIP("X"),
    HIT_EMPTY("O");

    private final String tile;

    TileStatus(String tile) {
        this.tile = tile;
    }

    @Override
    public String toString() {
        return this.tile;
    }
}
