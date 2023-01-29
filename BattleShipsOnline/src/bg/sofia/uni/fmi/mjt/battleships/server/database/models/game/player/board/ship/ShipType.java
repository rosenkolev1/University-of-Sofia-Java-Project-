package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.ship;

public enum ShipType {
    HUGE(5),
    LARGE(4),
    MEDIUM(3),
    SMALL(2);

    private final int size;

    ShipType(int size) {
        this.size = size;
    }

    public int size() {
        return size;
    }
}
