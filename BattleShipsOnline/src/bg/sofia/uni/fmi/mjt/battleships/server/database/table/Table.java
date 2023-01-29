package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import bg.sofia.uni.fmi.mjt.battleships.server.database.table.entry.TableEntryInfo;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class Table {
    protected final Path tablePath;
    protected final String entrySeparator;
    protected final String fieldSeparator;

    protected final Gson gson = new Gson();

    protected Table(Path tablePath, String entrySeparator, String fieldSeparator) {
        this.tablePath = tablePath;
        this.entrySeparator = entrySeparator;
        this.fieldSeparator = fieldSeparator;

        createTable();
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

    protected <T> void overwriteEntry(TableEntryInfo<T> entryInfo) {
        if (entryInfo.entryIndex != -1) {
            try (var bufferedWriter = Files.newBufferedWriter(tablePath)) {
                for (int i = 0; i < entryInfo.tableEntries.size(); i++) {
                    if (i != entryInfo.entryIndex) {
                        bufferedWriter.write(entryInfo.tableEntries.get(i) +  entrySeparator);
                    }
                    else {
                        boolean deletingEntry = entryInfo.entry == null;

                        if (!deletingEntry) {
                            var jsonEntry = gson.toJson(entryInfo.entry);
                            bufferedWriter.write(jsonEntry + entrySeparator);
                        }
                    }
                }

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected List<String> getTableEntries() {
        try (var bufferedReader = Files.newBufferedReader(tablePath)) {
            List<String> tableEntries = new ArrayList<>();

            StringBuilder entry = new StringBuilder();

            while (true) {
                var fileChar = bufferedReader.read();

                boolean endOfFileReached = fileChar == -1;

                var charString = String.valueOf((char)fileChar);

                boolean foundSeparator = foundEntrySeparator(charString, bufferedReader);

                if (endOfFileReached || foundSeparator){
                    var lineString = entry.toString();

                    if (lineString.isBlank()) {
                        break;
                    }

                    tableEntries.add(lineString);

                    entry = new StringBuilder();

                    if (endOfFileReached) {
                        break;
                    }
                }
                else {
                    entry.append(charString);
                }
            }
            return tableEntries;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean foundEntrySeparator(String charString, BufferedReader bufferedReader) throws IOException {
        //Check if the charString is the beginning of an entry separator
        boolean foundSeparator = entrySeparator.equals(charString);

        if (!foundSeparator && entrySeparator.startsWith(charString)) {

            bufferedReader.mark(entrySeparator.length());

            StringBuilder potentialSeparator = new StringBuilder(charString);

            while(true) {
                var tempFileChar = bufferedReader.read();

                if (tempFileChar == -1) {
                    foundSeparator = false;
                    break;
                }

                var tempCharString = String.valueOf((char)tempFileChar);
                potentialSeparator.append(tempCharString);
                var potentialSeparatorString = potentialSeparator.toString();

                if (!entrySeparator.startsWith(potentialSeparatorString)) {
                    foundSeparator = false;
                    break;
                }
                else if (entrySeparator.equals(potentialSeparatorString)) {
                    foundSeparator = true;
                    break;
                }
            }

            bufferedReader.reset();
        }

        if (foundSeparator) {
            for (int i = 0; i < entrySeparator.length() - 1; i++) {
                bufferedReader.read();
            }
        }

        return foundSeparator;
    }
}
