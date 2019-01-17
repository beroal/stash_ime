package ua.in.beroal.util;

import android.support.annotation.NonNull;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.UnicodeSet;

import io.reactivex.Observable;

import static ua.in.beroal.util.ReactiveX.rangeEdges;

public class Unicode {
    public enum CharPropertyType { BINARY, INTEGER, MASK, DOUBLE, STRING, OTHER }

    @SuppressWarnings("deprecation")
    public static Observable<Integer> getPropertyRange(CharPropertyType a) {
        switch (a) {
            case BINARY:
                return rangeEdges(UProperty.BINARY_START, UProperty.BINARY_LIMIT);
            case INTEGER:
                return rangeEdges(UProperty.INT_START, UProperty.INT_LIMIT);
            case MASK:
                return rangeEdges(UProperty.MASK_START, UProperty.MASK_LIMIT);
            case DOUBLE:
                return rangeEdges(UProperty.DOUBLE_START, UProperty.DOUBLE_LIMIT);
            case STRING:
                return rangeEdges(UProperty.STRING_START, UProperty.STRING_LIMIT);
            case OTHER:
                return rangeEdges(UProperty.OTHER_PROPERTY_START, UProperty.OTHER_PROPERTY_LIMIT);
        }
        throw new IllegalArgumentException("TODO");
    }

    public static Observable<Integer> getPropertyValueRange(int propertyId) {
        int start = UCharacter.getIntPropertyMinValue(propertyId);
        return Observable.range(start,
                UCharacter.getIntPropertyMaxValue(propertyId) + 1 - start);
    }

    public static Observable<Integer> getEntryRangeObservable(UnicodeSet.EntryRange range) {
        return rangeEdges(range.codepoint, range.codepointEnd);
    }
    @NonNull
    public static String codePointToString(int a) {
        return UCharacter.isBMP(a) ? new String(new char[] {(char) a})
                : new String(new char[] {Character.highSurrogate(a), Character.lowSurrogate(a)});
    }
    public static UnicodeSet standardCharSet() {
        return new UnicodeSet()
                .applyIntPropertyValue(UProperty.GENERAL_CATEGORY,
                        UCharacterCategory.SURROGATE)
                .addAll(new UnicodeSet()
                        .applyIntPropertyValue(UProperty.GENERAL_CATEGORY,
                                UCharacterCategory.UNASSIGNED))
                .addAll(new UnicodeSet()
                        .applyIntPropertyValue(UProperty.GENERAL_CATEGORY,
                                UCharacterCategory.PRIVATE_USE))
                .complement();
    }

    public static Observable<Integer> setToObservable(UnicodeSet set) {
        return Observable.fromIterable(set.ranges()).concatMap(Unicode::getEntryRangeObservable);
    }


    public static String getCharName(int a) {
        final String name = UCharacter.getName(a);
        return name == null ? "" : name;
    }

}