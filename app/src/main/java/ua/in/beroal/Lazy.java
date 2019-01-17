package ua.in.beroal;

import java8.util.function.Supplier;

public class Lazy<T> {
    private T value;
    private Supplier<T> initialValueF;

    public Lazy() {
    }

    public Lazy(Supplier<T> initialValueF) {
        this.initialValueF = initialValueF;
    }

    protected T initialValue() {
        if (initialValueF != null) {
            return initialValueF.get();
        } else {
            throw new RuntimeException("TODO");
        }
    }

    public T get() {
        if (value == null) {
            value = initialValue();
        }
        return value;
    }
}
