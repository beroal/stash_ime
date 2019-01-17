package ua.in.beroal.stash_ime;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodSubtype;

import java.util.List;
import java.util.Locale;

import java8.util.Optional;

public class Ime extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, LifecycleOwner {
    public static final String SUBTYPE_STRING_ID_EXTRA_KEY = "string_id";
    public static final String THIS_INPUT_METHOD_ID;

    static {
        Class<Ime> aClass = Ime.class;
        THIS_INPUT_METHOD_ID = BuildConfig.APPLICATION_ID + "/." + aClass.getSimpleName();
    }

    private LifecycleRegistry lifecycle;
    private MutableLiveData<Optional<KbKeys>> emptyKbLiveData;
    private MutableLiveData<Optional<String>> kbIdLiveData = new MutableLiveData<>();
    private LiveData<Optional<KbKeys>> kbKeysLiveData;

    public Ime() {
        lifecycle = new LifecycleRegistry(this);
        emptyKbLiveData = new MutableLiveData<>();
        emptyKbLiveData.setValue(Optional.empty());
        kbKeysLiveData = Transformations.switchMap(kbIdLiveData,
                kbId -> kbId.isEmpty() ? emptyKbLiveData
                        : ((App) getApplication()).getEditKbRepo().getKeysOptionalLiveData(kbId.orElseThrow()));
    }

    public static InputMethodSubtype[] inputMethodSubtypes(List<String> kbList) {
        final InputMethodSubtype[] subtypes = new InputMethodSubtype[kbList.size()];
        final InputMethodSubtype.InputMethodSubtypeBuilder subtypeBuilder =
                new InputMethodSubtype.InputMethodSubtypeBuilder()

                        .setSubtypeNameResId(R.string.im_subtype_name_dyn)
                        .setSubtypeMode("keyboard");
        int i = 0;
        final Locale[] locales = Locale.getAvailableLocales();
        for (String kbId : kbList) {

            subtypes[i] = subtypeBuilder
                    .setSubtypeId(kbId.hashCode())
                    .setSubtypeLocale(locales[i].toString())
                    .setSubtypeExtraValue("UntranslatableReplacementStringInSubtypeName=" + kbId
                            + "," + SUBTYPE_STRING_ID_EXTRA_KEY + "=" + kbId)
                    .build();
            i++;
        }
        return subtypes;
    }

    @Override
    public View onCreateInputView() {
        final KbView kbV = new KbView(this, null, 0, 0);
        kbV.setOnKeyboardActionListener(this);
        final InputMethodSubtype imSubtype = App.getInputMethodManager().get(this).getCurrentInputMethodSubtype();
        String kbId;
        if (imSubtype == null) {
            kbId = null;
        } else {

            final String subtypeKbId = imSubtype.getExtraValueOf(SUBTYPE_STRING_ID_EXTRA_KEY);
            kbId = ((App) getApplication()).getEditKbRepo().kbExists(subtypeKbId) ?
                    subtypeKbId : null;
        }
        kbIdLiveData.setValue(Optional.ofNullable(kbId));
        kbKeysLiveData.observe(this, kbV::setContents);
        /*TODO if chosen kb is deleted*/
        return kbV;
    }

    @Override
    public void onWindowShown() {
        lifecycle.markState(Lifecycle.State.RESUMED);
        super.onWindowShown();
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        getCurrentInputConnection().commitText(
                new String(new int[]{primaryCode}, 0, 1),
                1);

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    protected void onCurrentInputMethodSubtypeChanged(InputMethodSubtype newSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype);
        kbIdLiveData.setValue(Optional.of(newSubtype.getExtraValueOf(SUBTYPE_STRING_ID_EXTRA_KEY)));
        Log.d("App", "SubtypeChanged=" + newSubtype);
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        lifecycle.markState(Lifecycle.State.CREATED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycle;
    }
}
