package bg.sofia.uni.fmi.mjt.battleships.server.database.table;

import java.util.List;

public class TableEntryInfo<T> {
    public T entry;
    public int entryIndex;
    public List<String> tableEntries;

    public TableEntryInfo(T entry, int entryIndex, List<String> tableEntries) {
        this.entry = entry;
        this.entryIndex = entryIndex;
        this.tableEntries = tableEntries;
    }
}
