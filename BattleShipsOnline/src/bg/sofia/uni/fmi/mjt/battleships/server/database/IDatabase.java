package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.IUserTable;

public interface IDatabase {

    IUserTable userTable();

    IGameTable gameTable();

}
