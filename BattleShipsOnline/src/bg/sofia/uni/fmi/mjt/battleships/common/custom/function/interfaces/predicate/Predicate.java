package bg.sofia.uni.fmi.mjt.battleships.common.custom.function.interfaces.predicate;

@FunctionalInterface
public interface Predicate<T1, T2> {
    boolean test(T1 arg1, T2 arg2);
}
