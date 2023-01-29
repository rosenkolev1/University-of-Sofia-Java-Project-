package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.GameTable;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.UserTable;

public class Database {
    private static final String defaultEntrySeparator = "\n";
    private static final String defaultFieldSeparator = " ";

    public UserTable userTable;

    public GameTable gameTable;

    public Database(String usersPath, String gamesPath, String entrySeparator, String fieldSeparator) {
        this.userTable = new UserTable(usersPath, entrySeparator, fieldSeparator);
        this.gameTable = new GameTable(gamesPath, entrySeparator, fieldSeparator);
    }

    public Database(String usersPath, String gamesPath) {
        this(usersPath, gamesPath, defaultEntrySeparator, defaultFieldSeparator);
    }

}
