package bg.sofia.uni.fmi.mjt.battleships.server.dto;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.tile.TileStatus;

public enum BoardOption {
    NO_FOG(TileStatus.EMPTY),
    FOG(TileStatus.FOG);

    private final TileStatus defaultStatus;

    BoardOption(TileStatus defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public TileStatus defaultStatus() {
        return this.defaultStatus;
    }
}
