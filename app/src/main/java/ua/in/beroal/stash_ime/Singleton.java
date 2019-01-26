package ua.in.beroal.stash_ime;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.List;

import ua.in.beroal.android.LazyWithContext;

public class Singleton {
    private static LazyWithContext<InputMethodManager> inputMethodManager =
            new LazyWithContext<>(
                    context -> {
                        final InputMethodManager r =
                                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (r == null) {
                            throw new IllegalStateException("no INPUT_METHOD_SERVICE/InputMethodManager");
                        } else {
                            return r;
                        }
                    });
    private static LazyWithContext<String> thisInputMethodId =
            new LazyWithContext<>(
                    context -> {
                        String imId = null;
                        for (InputMethodInfo imInfo :
                                getInputMethodManager().get(context).getInputMethodList()) {
                            if (BuildConfig.APPLICATION_ID.equals(imInfo.getPackageName()
                                    /* In reality, {@code InputMethodInfo.getPackageName} returns the application ID, not the package of {@link InputMethodService}. */)) {
                                if (imId == null) {
                                    imId = imInfo.getId();
                                } else {
                                    throw new IllegalStateException("There is more than 1 input method in this application.");
                                }
                            }
                        }
                        return imId;
                    }
            );
    private static LazyWithContext<EditKbRepo> editKbRepo =
                    new LazyWithContext<>(
                            context -> {
                                EditKbRepo editKbRepo1 = null;
                                try {
                                    editKbRepo1 = new EditKbRepo(context,
                                            kbList -> sendKbListToOs(context, kbList));
                                } catch (IOException e) {
                                    Log.e("App", "I/O error", e);
                                }
                                return editKbRepo1;
                            });

    public static LazyWithContext<InputMethodManager> getInputMethodManager() {
        return inputMethodManager;
    }

    public static LazyWithContext<String> getThisInputMethodId() {
        return thisInputMethodId;
    }

    public static LazyWithContext<EditKbRepo> getEditKbRepo() {
        return editKbRepo;
    }

    public static void sendKbListToOs(Context context, List<String> kbList) {
        Log.d("App", "sendKbListToOs=" + kbList);
        getInputMethodManager().get(context)
                .setAdditionalInputMethodSubtypes(
                        getThisInputMethodId().get(context),
                        Ime.inputMethodSubtypes(kbList));
        Log.d("App", "sendKbListToOs done");
    }
}
