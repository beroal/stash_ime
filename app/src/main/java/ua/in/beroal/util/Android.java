package ua.in.beroal.util;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
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
    public static void charSequenceToClipboard(Context context, CharSequence text) {
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("", text));
    }

    public static CharSequence clipboardToCharSequence(Context context) {
        final ClipData clip = ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .getPrimaryClip();
        return clip.getItemCount() == 0 ? null : clip.getItemAt(0).getText();
    }

    public static int clipboardToChar(Context context) {
        final CharSequence a = clipboardToCharSequence(context);
        return a == null || a.length() == 0 ? -1 : UCharacter.codePointAt(a, 0);
    }

    public static void charToClipboard(Context context, int char1) {
        charSequenceToClipboard(context, Unicode.codePointToString(char1));
    }

    public static View inflateDoNotAttach(int r, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(r, parent, false);
    }



    @MainThread
    public static <A0, A1, R> LiveData<R> map2(
            @NonNull LiveData<A0> source0, @NonNull LiveData<A1> source1,
            @NonNull final BiFunction<A0, A1, R> func) {
        final MediatorLiveData<R> result = new MediatorLiveData<>();
        result.addSource(source0, new Observer<A0>() {
            @Override
            public void onChanged(@Nullable A0 a0) {
                A1 a1 = source1.getValue();
                if (a1 != null) {
                    result.setValue(func.apply(a0, a1));
                }

            }
        });
        result.addSource(source1, new Observer<A1>() {
            @Override
            public void onChanged(@Nullable A1 a1) {
                A0 a0 = source0.getValue();
                if (a0 != null) {
                    result.setValue(func.apply(a0, a1));
                }
            }
        });
        return result;
    }
}
