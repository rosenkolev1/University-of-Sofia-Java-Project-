package bg.sofia.uni.fmi.mjt.battleships.server.database;

import bg.sofia.uni.fmi.mjt.battleships.server.database.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Database {

    private String entrySeparator = "\n";
    private String fieldSeparator = " ";

    private Path usersTablePath = Path.of("users.txt");

    private List<User> users;

    public Database(String usersPath) {
        usersTablePath = Path.of(usersPath);
        this.users = new ArrayList<>();
        this.entrySeparator = "\n";

        initializeUsers();
    }

    public List<User> getUsers() {
        var copyList = new ArrayList<User>();

        for (var user : users) {
            copyList.add(user.clone());
        }

        return copyList;
    }

    public void addUser(String username, String password) {
        try (var bufferedWriter = Files.newBufferedWriter(usersTablePath, StandardOpenOption.APPEND)) {
            //TODO: Add hashing with salt to passwords
            var newUser = new User(username, password);
            this.users.add(newUser);

            bufferedWriter.append(username + " " + password + entrySeparator);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeUsers() {
        if (!Files.isRegularFile(usersTablePath)) {
            try {
                Files.createFile(usersTablePath);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (var bufferedReader = Files.newBufferedReader(usersTablePath)) {

            while(true) {
                var line = bufferedReader.readLine();

                if (line == null) {
                    break;
                }

                //TODO: Add hashing with salt to passwords

                var fields = line.split(fieldSeparator);
                var username = fields[0];
                var password = fields[1];

                this.users.add(new User(username, password));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
