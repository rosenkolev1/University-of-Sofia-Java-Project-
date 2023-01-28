package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

public enum GameStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    PAUSED("Paused"),
    ENDED("Ended"),
    ABANDONED("Abandoned");

    private final String status;

    GameStatus(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }
}
