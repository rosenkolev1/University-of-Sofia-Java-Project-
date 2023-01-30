package bg.sofia.uni.fmi.mjt.battleships.server.database.table.user;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public interface IUserTable {
    User getUser(String username);

    void addUser(User user);

    void addUser(String username, String password);

    boolean userExists(User user);

    boolean userExistWithName(String username);

    boolean userExistWithPassword(String password);
}
