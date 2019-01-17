package ua.in.beroal.stash_ime;

import com.ibm.icu.lang.UProperty;

import java.util.Arrays;

import io.reactivex.Observable;

import static ua.in.beroal.util.ReactiveX.filterObservableContainsS;
import static ua.in.beroal.util.Unicode.getPropertyRange;
import static ua.in.beroal.util.Unicode.getPropertyValueRange;

public class Unicode {
    public static Observable<Cpv> filteredCpvObservable(Iterable<String> words) {
        return filterObservableContainsS(words,
                a -> {
                    final String valueName = a.getValueName(UProperty.NameChoice.LONG);
                    return Arrays.asList(valueName == null ? "" : valueName,
                            a.getPropertyName(UProperty.NameChoice.LONG));
                },
                cpvObservable());
    }

    public static Observable<Cpv> cpvObservable() {
        final Observable<Integer> propertyList = Observable.concat(
                getPropertyRange(ua.in.beroal.util.Unicode.CharPropertyType.BINARY),
                getPropertyRange(ua.in.beroal.util.Unicode.CharPropertyType.INTEGER));
        return propertyList
                .concatMap(propertyId -> getPropertyValueRange(propertyId)
                        .map(valueId -> new Cpv(propertyId, valueId)));
    }
}
