package ua.in.beroal.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import io.reactivex.Observable;
import java8.util.function.Function;

import static ua.in.beroal.util.Java.containsMany;

public class ReactiveX {
    @NonNull
    public static <E> Observable<E> filterObservableContainsS(
            @NonNull Iterable<String> needles,
            @NonNull Function<E, Iterable<String>> f,
            @NonNull Observable<E> xs) {
        return xs.filter(e -> containsMany(f.apply(e), needles));
    }

    @NonNull
    public static Observable<Integer> rangeEdges(int start, int end) {
        return Observable.range(start, end - start);
    }

    @NonNull
    public static <E> ArrayList<E> observableToArrayList(@NonNull Observable<? extends E> a) {
        ArrayList<E> r = new ArrayList<>();
        a.subscribe(r::add);
        return r;
    }

}
