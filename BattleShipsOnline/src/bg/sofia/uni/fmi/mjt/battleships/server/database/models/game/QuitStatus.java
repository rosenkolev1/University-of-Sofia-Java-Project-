package bg.sofia.uni.fmi.mjt.battleships.server.database.models.game;

public enum QuitStatus {
    NONE(0, "None"),
    ABANDON(1, "Abandoned"),
    SAVE_AND_QUIT(2, "Saved");

    private final int statusCode;
    private final String statusName;

    QuitStatus(int statusCode, String statusName) {
        this.statusCode = statusCode;
        this.statusName = statusName;
    }

    public static QuitStatus getByCode(int statusCode) {
        for (var status : QuitStatus.values()) {
            if (status.statusCode == statusCode) {
                return status;
            }
        }

        return null;
    }

    public int statusCode() {
        return statusCode;
    }

    public String statusName() {
        return this.statusName;
    }
}
