package ua.in.beroal.stash_ime;

import android.app.Application;
import android.content.SharedPreferences;

import ua.in.beroal.Lazy;

public class App extends Application {
    public static final String NON_FIRST_RUN_PREF_FIELD = "non_first_run";
    private Lazy<CharClipboardRepo> charClipboardRepo =
            new Lazy<>(() -> new CharClipboardRepo(this));

    public Lazy<CharClipboardRepo> getCharClipboardRepo() {
        return charClipboardRepo;
    }

    public EditKbRepo getEditKbRepo() {
        return Singleton.getEditKbRepo().get(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences sharedPreferences = getSharedPreferences(
                BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        /* If this is the first run of this application on this device,
         * the OS does not know about user's keyboards,
         * so the subtypes need to be sent to OS by creating a {@link EditKbRepo}. */
        if (!sharedPreferences.getBoolean(NON_FIRST_RUN_PREF_FIELD, false)) {
            getEditKbRepo();
            sharedPreferences.edit().putBoolean(NON_FIRST_RUN_PREF_FIELD, true).apply();
        }
    }
}
