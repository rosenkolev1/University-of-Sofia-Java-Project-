package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

public enum QuitStatus {
    NONE(0),
    ABANDON(1),
    SAVE_AND_QUIT(2);

    private final int statusCode;

    QuitStatus(int statusCode) {
        this.statusCode = statusCode;
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
}
