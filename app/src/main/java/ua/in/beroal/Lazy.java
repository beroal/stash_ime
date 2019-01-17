package ua.in.beroal;

import java8.util.function.Supplier;

/**
 * An object of class {@code T} that is created lazily.
 */
public class Lazy<T> {
    private T value;
    private Supplier<T> initialValueF;

    public Lazy() {
    }

    public Lazy(Supplier<T> initialValueF) {
        this.initialValueF = initialValueF;
    }

    protected T initialValue() {
        if (initialValueF == null) {
            throw new IllegalStateException("initialValue is not overriden and a constructor argument was null.");
        } else {
            return initialValueF.get();
        }
    }

    public T get() {
        if (value == null) {
            value = initialValue();
        }
        return value;
    }
}
