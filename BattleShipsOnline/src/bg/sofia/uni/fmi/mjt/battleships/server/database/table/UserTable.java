package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class UserTable extends Table {

    private List<User> users;

    public UserTable(Path tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseUsers();
    }

    public UserTable(String tablePath, String entrySeparator, String fieldSeparator) {
        super(tablePath, entrySeparator, fieldSeparator);
        initialiseUsers();
    }

    public User getUser(String username) {
        for (var user : users) {
            if (user.username().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public List<User> getUsers() {
        var copyList = new ArrayList<User>();

        for (var user : users) {
            copyList.add(user.clone());
        }

        return copyList;
    }

    public void addUser(User user) {
        try (var bufferedWriter = Files.newBufferedWriter(tablePath, StandardOpenOption.APPEND)) {
            //TODO: Add hashing with salt to passwords
            this.users.add(user);

            var userJson = gson.toJson(user);
            bufferedWriter.append(userJson + entrySeparator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addUser(String username, String password) {
        addUser(new User(username, password));
    }

    public boolean userExistWithName(String username) {
        return this.users.stream().anyMatch(x -> x.username().equals(username));
    }

    public boolean userExistWithPassword(String password) {
        return this.users.stream().anyMatch(x -> x.password().equals(password));
    }

    private void initialiseUsers() {
        this.users = new ArrayList<>();

        if (!Files.isRegularFile(tablePath)) {
            try {
                Files.createFile(tablePath);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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
