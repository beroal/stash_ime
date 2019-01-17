package ua.in.beroal.util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibm.icu.lang.UCharacter;

import java8.util.function.BiFunction;

public class Android {
    public static void charSequenceToClipboard(@NonNull Context context, CharSequence text) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("", text));
    }

    @Nullable
    public static CharSequence clipboardToCharSequence(@NonNull Context context) {
        final ClipData clip =
                ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                        .getPrimaryClip();
        return clip.getItemCount() == 0 ? null : clip.getItemAt(0).getText();
    }

    public static void charToClipboard(@NonNull Context context, int char1) {
        charSequenceToClipboard(context, Unicode.codePointToString(char1));
    }

    /**
     * @return The first character of the text in the OS clipboard.
     */
    public static int clipboardToChar(@NonNull Context context) {
        final CharSequence a = clipboardToCharSequence(context);
        return a == null || a.length() == 0
                ? Unicode.NO_CHAR : UCharacter.codePointAt(a, 0);
    }


    public static View inflateDoNotAttach(int resId, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(resId, parent, false);
    }


    @MainThread
    public static <A0, A1, R> LiveData<R> map2(
            @NonNull LiveData<A0> source0, @NonNull LiveData<A1> source1,
            @NonNull final BiFunction<A0, A1, R> func) {
        final MediatorLiveData<R> result = new MediatorLiveData<>();
        result.addSource(source0, a0 -> {
            A1 a1 = source1.getValue();
            if (a1 != null) {
                result.setValue(func.apply(a0, a1));
            }

        });
        result.addSource(source1, a1 -> {
            A0 a0 = source0.getValue();
            if (a0 != null) {
                result.setValue(func.apply(a0, a1));
            }
        });
        return result;
    }
}
