package bg.sofia.uni.fmi.mjt.battleships.common.custom.function.interfaces.function;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface Function<T1, T2, R> {
    R apply(T1 arg1, T2 arg2) throws InvocationTargetException, IllegalAccessException;
}
