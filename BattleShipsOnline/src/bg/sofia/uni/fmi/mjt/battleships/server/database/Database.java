package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IGameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.IUserTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.UserTable;

public class Database implements IDatabase {
//    private static final String defaultEntrySeparator = "\n";
//    private static final String defaultFieldSeparator = " ";
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

//    public Database(IUserTable userTable, IGameTable gameTable, String usersPath, String gamesPath, String entrySeparator, String fieldSeparator) {
////        this.userTable = new UserTable(usersPath, entrySeparator, fieldSeparator);
////        this.gameTable = new GameTable(gamesPath, entrySeparator, fieldSeparator);
//
//    }

//    public Database(String usersPath, String gamesPath) {
//        this(usersPath, gamesPath, defaultEntrySeparator, defaultFieldSeparator);
//    }

}
