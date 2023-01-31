package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.game.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.user.IUserTable;

public class Database implements IDatabase {
    private IUserTable userTable;

    private IGameTable gameTable;

    public Database(IUserTable userTable, IGameTable gameTable) {
        this.userTable = userTable;
        this.gameTable = gameTable;
    }

    @Override
    public IUserTable userTable() {
        return this.userTable;
    }

    @Override
    public IGameTable gameTable() {
        return this.gameTable;
    }

}
