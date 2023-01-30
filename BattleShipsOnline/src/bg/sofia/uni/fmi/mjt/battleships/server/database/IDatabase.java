package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IUserTable;

public interface IDatabase {

    IUserTable userTable();

    IGameTable gameTable();

}
