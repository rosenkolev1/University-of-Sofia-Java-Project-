package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import bg.sofia.uni.fmi.mjt.battleships.common.SessionCookie;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Ship {

    public final List<TilePos> tiles;

    public final ShipType type;

    public ShipStatus status;

    public Ship(List<TilePos> tiles, ShipType type, ShipStatus status) {
        this.tiles = tiles;
        this.type = type;
        this.status = status;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof Ship castOther)) {
            return false;
        }

        return this.type.equals(castOther.type) && this.status.equals(castOther.status) &&
            new HashSet<>(this.tiles).containsAll(castOther.tiles) &&
            new HashSet<>(castOther.tiles).containsAll(this.tiles);
    }

}
