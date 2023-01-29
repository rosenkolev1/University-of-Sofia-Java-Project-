package bg.sofia.uni.fmi.mjt.battleships.common;

@FunctionalInterface
public interface Predicate<T1, T2> {
    boolean test(T1 arg1, T2 arg2);
}
