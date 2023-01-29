package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.tile;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.player.board.BoardRank;

public record TilePos(BoardRank rank, int file) {
}
