package ua.in.beroal.android;

import android.content.Context;

import java8.util.function.Function;

/**
 * An object of class {@code T} that is created lazily from a {@link Context}.
 */
public class LazyWithContext<T> {
    private T value;
    private Function<Context, T> initialValueF;

    public LazyWithContext() {
    }

    public LazyWithContext(Function<Context, T> initialValueF) {
        this.initialValueF = initialValueF;
    }

    protected T initialValue(Context context) {
        if (initialValueF == null) {
            throw new IllegalStateException("initialValue is not overriden and a constructor argument was null.");
        } else {
            return initialValueF.apply(context);
        }
    }

    public T get(Context context) {
        if (value == null) {
            value = initialValue(context);
        }
        return value;
    }
}
