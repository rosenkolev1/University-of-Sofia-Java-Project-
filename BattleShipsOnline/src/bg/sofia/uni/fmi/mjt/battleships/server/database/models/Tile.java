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
}
