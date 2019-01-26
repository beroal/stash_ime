package ua.in.beroal.stash_ime.test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;

import ua.in.beroal.stash_ime.BuildConfig;
import ua.in.beroal.stash_ime.R;
import ua.in.beroal.stash_ime.Singleton;

import static ua.in.beroal.stash_ime.App.NON_FIRST_RUN_PREF_FIELD;

public class TestActivity extends AppCompatActivity {
    private InputMethodManager imm;

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = Singleton.getInputMethodManager().get(this);
        final String thisInputMethodId = Singleton.getThisInputMethodId().get(this);
        Log.d("App", "thisInputMethodId=" + thisInputMethodId);
        setContentView(R.layout.activity_test);
        ((Button) findViewById(R.id.im_settings)).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.subtype_enabler)).setOnClickListener(
                v -> imm.showInputMethodAndSubtypeEnabler(thisInputMethodId));
        ((Button) findViewById(R.id.add_subtypes)).setOnClickListener(v -> {
            imm.setAdditionalInputMethodSubtypes(thisInputMethodId, new InputMethodSubtype[]{
                    new InputMethodSubtype.InputMethodSubtypeBuilder()
                            .setSubtypeNameResId(R.string.im_subtype_name_dyn)
                            .setSubtypeMode("keyboard")
                            .setSubtypeLocale("en_US")
                            .setSubtypeExtraValue("UntranslatableReplacementStringInSubtypeName=prg")
                            .build(),
                    new InputMethodSubtype.InputMethodSubtypeBuilder()
                            .setSubtypeNameResId(R.string.im_subtype_name_dyn)
                            .setSubtypeMode("keyboard")
                            .setSubtypeLocale("uk_UA")
                            .build()
            });
        });
        ((Button) findViewById(R.id.delete_subtypes)).setOnClickListener(
                v -> imm.setAdditionalInputMethodSubtypes(
                        thisInputMethodId, new InputMethodSubtype[]{}));
        ((Button) findViewById(R.id.reset_non_first_run_flag)).setOnClickListener(v -> {
            getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
                    .edit().putBoolean(NON_FIRST_RUN_PREF_FIELD, false).commit();
            System.exit(0);
        });
    }
}