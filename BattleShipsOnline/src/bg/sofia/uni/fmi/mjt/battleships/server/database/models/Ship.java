package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.List;

public record Ship(List<TilePos> tiles, ShipType type, ShipStatus status) {
}
