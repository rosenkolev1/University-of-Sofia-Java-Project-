package bg.sofia.uni.fmi.mjt.battleships.common.list;

import java.util.List;
import java.util.Objects;

public class ListUtil {
    public static <T> boolean haveSameElements(List<T> first, List<T> second) {
        return first == second ||
            (!(first == null || second == null) && Objects.equals(first.stream().sorted(), second.stream().sorted()));
    }
}
