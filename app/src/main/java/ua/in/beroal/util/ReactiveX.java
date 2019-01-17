package ua.in.beroal.util;

import java.util.ArrayList;

import io.reactivex.Observable;
import java8.util.function.Function;

import static ua.in.beroal.util.Java.containsMany;

public class ReactiveX {
    public static <E> Observable<E> filterObservableContainsS(
            Iterable<String> needles, Function<E, Iterable<String>> f, Observable<E> xs) {
        return xs.filter(e -> containsMany(f.apply(e), needles));
    }

    public static Observable<Integer> rangeEdges(int start, int end) {
        return Observable.range(start, end - start);
    }

    public static <E> ArrayList<E> observableToArrayList(Observable<? extends E> a) {
        ArrayList<E> r = new ArrayList<>();
        a.subscribe(r::add);
        return r;
    }

}
