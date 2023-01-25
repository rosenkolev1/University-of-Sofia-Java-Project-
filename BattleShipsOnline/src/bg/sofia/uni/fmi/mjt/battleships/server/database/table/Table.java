package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Table {
    protected final Path tablePath;
    protected final String entrySeparator;
    protected final String fieldSeparator;

    protected final Gson gson = new Gson();

    protected Table(Path tablePath, String entrySeparator, String fieldSeparator) {
        this.tablePath = tablePath;
        this.entrySeparator = entrySeparator;
        this.fieldSeparator = fieldSeparator;
    }

    protected Table(String tablePath, String entrySeparator, String fieldSeparator) {
        this(Path.of(tablePath), entrySeparator, fieldSeparator);
    }

    protected void createTable() {
        if (!Files.isRegularFile(tablePath)) {
            try {
                Files.createFile(tablePath);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
