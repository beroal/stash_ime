package ua.in.beroal.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Java {
    private static Pattern spacePattern = Pattern.compile("\\s+");

    @NonNull
    public static ArrayList<String> splitWords(@NonNull CharSequence wordsS) {
        ArrayList<String> words = new ArrayList<>();
        for (String s : spacePattern.split(wordsS, -1)) {
            if (!s.isEmpty()) {
                words.add(s);
            }
        }
        return words;
    }

    public static boolean containsIgnoreCase(@NonNull String src, @NonNull String what) {
        final int length = what.length();
        if (length == 0) {
            return true; // Empty string is contained
        }

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp) {
                continue;
            }

            if (src.regionMatches(true, i, what, 0, length)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return For every element {@code y} of {@code ys}, there is an element {@code x} of {@code xs}
     * such that {@code x} contains {@code y} ignoring character case.
     */
    public static boolean containsMany(@NonNull Iterable<String> xs, @NonNull Iterable<String> ys) {
        boolean r = true;
        for (String y : ys) {
            boolean r1 = false;
            for (String x : xs) {
                if (containsIgnoreCase(x, y)) {
                    r1 = true;
                    break;
                }
            }
            if (!r1) {
                r = false;
                break;
            }
        }
        return r;
    }
}
