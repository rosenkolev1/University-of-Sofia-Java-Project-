package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

public class Tile {

    private TilePos pos;
    public TileStatus status;

    public Tile(BoardRank rank, int file, TileStatus status) {
        this.pos = new TilePos(rank, file);
        this.status = status;
    }

    public TilePos pos() {
        return this.pos;
    }

    public boolean isHit() {
        return this.status.equals(TileStatus.HIT_EMPTY) ||
            this.status.equals(TileStatus.HIT_SHIP);
    }

    public void hitTile() {
        switch (this.status) {
            case EMPTY, HIT_EMPTY -> this.status = TileStatus.HIT_EMPTY;
            case SHIP, HIT_SHIP -> this.status = TileStatus.HIT_SHIP;
        }
    }
}
