package ua.in.beroal.stash_ime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ua.in.beroal.Lazy;
import ua.in.beroal.android.LazyWithContext;

import static ua.in.beroal.stash_ime.Ime.THIS_INPUT_METHOD_ID;

public class App extends Application {
    public static final String NON_FIRST_RUN_FIELD = "non_first_run";
    private static LazyWithContext<InputMethodManager> inputMethodManager = new LazyWithContext<>(
            context -> {
                final InputMethodManager r = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (r == null) {
                    Log.e("App", "no INPUT_METHOD_SERVICE/InputMethodManager");
                    System.exit(200);
                    throw new IllegalStateException();
                } else {
                    return r;
                }
            });
    private static LazyWithContext<EditKbRepo> editKbRepo = new LazyWithContext<>(
            context -> {
                final Pair<EditKbRepo, IOException> a =
                        EditKbRepo.create(context, kbList -> sendKbListToOs(context, kbList));
                final IOException e = a.second;
                if (e != null) {
                    Log.e("App", "I/O error", e);
                }
                return a.first;
            });
    private static LazyWithContext<String> thisInputMethodId = new LazyWithContext<>(
            context -> {
                String imId = null;
                for (InputMethodInfo imInfo :
                        getInputMethodManager().get(context).getInputMethodList()) {
                    if (BuildConfig.APPLICATION_ID.equals(imInfo.getPackageName()
                            /* in reality, {@code InputMethodInfo.getPackageName()} returns the application ID, not the package of {@code InputMethodService} */)) {
                        if (imId != null) {
                            Log.e("App", "more than 1 input method in this application");
                            System.exit(200);
                        } else {
                            imId = imInfo.getId();
                        }

                    }
                }
                return imId;
            }
    );
    private Lazy<CharClipboardRepo> charClipboardRepo =
            new Lazy<>(() -> new CharClipboardRepo(this));
    /*public Lazy<ExecutorService> executor = new Lazy<>(() -> Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()));*/

    public static LazyWithContext<EditKbRepo> getEditKbRepoContext() {
        return editKbRepo;
    }

    public static LazyWithContext<String> getThisInputMethodId() {
        return thisInputMethodId;
    }

    public static LazyWithContext<InputMethodManager> getInputMethodManager() {
        return inputMethodManager;
    }

    public Lazy<CharClipboardRepo> getCharClipboardRepo() {
        return charClipboardRepo;
    }

    /*public Lazy<ExecutorService> getExecutor() {
        return executor;
    }*/

    @NonNull
    public static void sendKbListToOs(Context context, List<String> kbList) {
        Log.d("App", "sendKbListToOs=" + kbList);
        getInputMethodManager().get(context)
                .setAdditionalInputMethodSubtypes(THIS_INPUT_METHOD_ID,
                        Ime.inputMethodSubtypes(kbList));
        Log.d("App", "sendKbListToOs done");
    }

    public EditKbRepo getEditKbRepo() {
        return getEditKbRepoContext().get(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences sharedPreferences = getSharedPreferences(
                BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(NON_FIRST_RUN_FIELD, false)) {
            getEditKbRepo();
            sharedPreferences.edit().putBoolean(NON_FIRST_RUN_FIELD, true).apply();
        }
    }
}
