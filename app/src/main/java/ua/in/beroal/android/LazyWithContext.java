package ua.in.beroal.android;

import android.content.Context;

import java8.util.function.Function;

public class LazyWithContext<T> {
    private T value;
    private Function<Context, T> initialValueF;

    public LazyWithContext() {
    }

    public LazyWithContext(Function<Context, T> initialValueF) {
        this.initialValueF = initialValueF;
    }

    protected T initialValue(Context context) {
        if (initialValueF != null) {
            return initialValueF.apply(context);
        } else {
            throw new RuntimeException("TODO");
        }
    }

    public T get(Context context) {
        if (value == null) {
            value = initialValue(context);
        }
        return value;
    }
}
