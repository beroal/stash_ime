package ua.in.beroal.stash_ime.test;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;

import ua.in.beroal.stash_ime.App;
import ua.in.beroal.stash_ime.BuildConfig;
import ua.in.beroal.stash_ime.R;

import static ua.in.beroal.stash_ime.App.NON_FIRST_RUN_FIELD;
import static ua.in.beroal.stash_ime.Ime.THIS_INPUT_METHOD_ID;

public class TestActivity extends AppCompatActivity {

    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = App.getInputMethodManager().get(this);
        Log.d("App", "THIS_INPUT_METHOD_ID=" + THIS_INPUT_METHOD_ID);
        Log.d("App", "thisInputMethodId=" + App.getThisInputMethodId().get(this));

        setContentView(R.layout.activity_test);
        ((Button) findViewById(R.id.im_settings)).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.subtype_enabler)).setOnClickListener(v -> {
            String thisInputMethodId = null;
            for (InputMethodInfo imInfo : imm.getEnabledInputMethodList()) {
                if (getPackageName().equals(imInfo.getPackageName())) {
                    thisInputMethodId = imInfo.getId();
                    break;
                }
            }
            Log.d("App", "thisInputMethodId=" + thisInputMethodId);
            /*final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
            intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, thisInputMethodId);
            intent.putExtra(Intent.EXTRA_TITLE, "test title");
            startActivity(intent);*/
            imm.showInputMethodAndSubtypeEnabler(thisInputMethodId);
        });
        ((Button) findViewById(R.id.add_subtypes)).setOnClickListener(v -> {

            imm.setAdditionalInputMethodSubtypes(THIS_INPUT_METHOD_ID, new InputMethodSubtype[]{
                    new InputMethodSubtype.InputMethodSubtypeBuilder()
                            .setSubtypeNameResId(R.string.im_subtype_name_dyn)
                            .setSubtypeMode("keyboard")
                            .setSubtypeLocale("en_US")
                            .setSubtypeExtraValue("UntranslatableReplacementStringInSubtypeName=prg")
                            .build(),
                    new InputMethodSubtype.InputMethodSubtypeBuilder()
                            .setSubtypeNameResId(R.string.im_test_name_dynamic_1)
                            .setSubtypeMode("keyboard")
                            .setSubtypeLocale("uk_UA")
                            .build()
            });
        });
        ((Button) findViewById(R.id.delete_subtypes)).setOnClickListener(v -> {
            imm.setAdditionalInputMethodSubtypes(THIS_INPUT_METHOD_ID, new InputMethodSubtype[]{});
        });
        ((Button) findViewById(R.id.reset_non_first_run_flag)).setOnClickListener(v ->
        {
            getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
                    .edit().putBoolean(NON_FIRST_RUN_FIELD, false).commit();

            System.exit(0);
        });

        /*showInputMethodPicker*/
    }

}