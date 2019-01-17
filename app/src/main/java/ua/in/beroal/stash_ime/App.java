package ua.in.beroal.stash_ime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.List;

import ua.in.beroal.Lazy;
import ua.in.beroal.android.LazyWithContext;

public class App extends Application {
    public static final String NON_FIRST_RUN_FIELD = "non_first_run";
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
    private Lazy<CharClipboardRepo> charClipboardRepo =
            new Lazy<>(() -> new CharClipboardRepo(this));

    public static LazyWithContext<InputMethodManager> getInputMethodManager() {
        return inputMethodManager;
    }

    public static LazyWithContext<String> getThisInputMethodId() {
        return thisInputMethodId;
    }

    public static LazyWithContext<EditKbRepo> getEditKbRepoContext() {
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

    public Lazy<CharClipboardRepo> getCharClipboardRepo() {
        return charClipboardRepo;
    }

    public EditKbRepo getEditKbRepo() {
        return getEditKbRepoContext().get(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences sharedPreferences = getSharedPreferences(
                BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        /* If this is the first run of this application on this device,
         * the OS does not know about user's keyboards,
         * and a {@link EditKbRepo} must be created. */
        if (!sharedPreferences.getBoolean(NON_FIRST_RUN_FIELD, false)) {
            getEditKbRepo();
            sharedPreferences.edit().putBoolean(NON_FIRST_RUN_FIELD, true).apply();
        }
    }
}
