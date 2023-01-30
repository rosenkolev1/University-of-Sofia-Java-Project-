package bg.sofia.uni.fmi.mjt.battleships.server.database.table.user;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.user.User;
import bg.sofia.uni.fmi.mjt.battleships.server.database.table.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class UserTable extends Table<User> implements IUserTable {
    private List<User> users;

    @Override
    public List<User> entries() {
        return this.users;
    }

    public UserTable(String tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseUsers();
    }

    @Override
    public User getUser(String username) {
        for (var user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public void addUser(User user) {
        try (var bufferedWriter = Files.newBufferedWriter(tablePath, StandardOpenOption.APPEND)) {
            //TODO: Add hashing with salt to passwords
            this.users.add(user);

            var userJson = gson.toJson(user);
            bufferedWriter.append(userJson).append(entrySeparator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUser(String username, String password) {
        addUser(new User(username, password));
    }

    @Override
    public boolean userExists(User user) {
        return this.users.contains(user);
    }

    @Override
    public boolean userExistWithName(String username) {
        return this.users.stream().anyMatch(x -> x.username().equals(username));
    }

    @Override
    public boolean userExistWithPassword(String password) {
        return this.users.stream().anyMatch(x -> x.password().equals(password));
    }

    private void initialiseUsers() {
        this.users = new ArrayList<>();

        try (var bufferedReader = Files.newBufferedReader(tablePath)) {

            while(true) {
                var line = bufferedReader.readLine();

                if (line == null) {
                    break;
                }

                //TODO: Add hashing with salt to passwords
                var user = gson.fromJson(line, User.class);
                this.users.add(user);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
