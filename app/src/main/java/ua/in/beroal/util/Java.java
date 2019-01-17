package ua.in.beroal.util;

import android.support.annotation.NonNull;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.StringSearch;

import java.util.ArrayList;

public class Java {
    @NonNull
    public static ArrayList<String> splitWords(String wordsS) {
        ArrayList<String> words = new ArrayList<>();
        for (String s : wordsS.split("\\s+", -1)) {
            if (! s.isEmpty()) {
                words.add(s);
            }
        }
        return words;
    }



    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    public static boolean containsMany(Iterable<String> xs, Iterable<String> ys) {
        boolean r = true;
        for (String y : ys) {
            boolean r1 = false;
            for (String x : xs) {
                if (containsIgnoreCase(x, y)) {
                    r1 = true;
                    break;
                }
            }
            if (! r1) {
                r = false;
                break;
            }
        }
        return r;
    }
}
