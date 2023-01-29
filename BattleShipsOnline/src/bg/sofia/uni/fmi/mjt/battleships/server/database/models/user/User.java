package bg.sofia.uni.fmi.mjt.battleships.server.database.models.user;

public record User(String username, String password) {

    private User(User other) {
        this(other.username, other.password);
    }
}
