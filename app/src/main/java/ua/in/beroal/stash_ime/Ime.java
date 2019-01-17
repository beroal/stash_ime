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
    public static final String SUBTYPE_STRING_ID_EXTRA_FIELD = "string_id";
    private LifecycleRegistry lifecycle;
    private MutableLiveData<Optional<KbKeys>> emptyKb = new MutableLiveData<>();
    private MutableLiveData<Optional<String>> kbId = new MutableLiveData<>();
    private LiveData<Optional<KbKeys>> kbKeys;

    public Ime() {
        lifecycle = new LifecycleRegistry(this);
        emptyKb.setValue(Optional.empty());
        kbKeys = Transformations.switchMap(kbId,
                kbId -> kbId.isEmpty()
                        ? emptyKb
                        : ((App) getApplication()).getEditKbRepo()
                        .getKeysOptional(kbId.orElseThrow()));
    }

    @NonNull
    public static InputMethodSubtype[] inputMethodSubtypes(@NonNull List<String> kbList) {
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
                    /* We use an undocumented feature of the OS input method framework.
                     * The value of the "Untranslatable..." field
                     * replaces "%s" in the input method subtype's name. */
                    .setSubtypeExtraValue("UntranslatableReplacementStringInSubtypeName=" + kbId
                            + "," + SUBTYPE_STRING_ID_EXTRA_FIELD + "=" + kbId)
                    .build();
            i++;
        }
        return subtypes;
    }

    @Override
    public View onCreateInputView() {
        final KbView kbView = new KbView(this, null, 0);
        kbView.setOnKeyboardActionListener(this);
        final InputMethodSubtype imSubtype = App.getInputMethodManager().get(this)
                .getCurrentInputMethodSubtype();
        String kbId;
        if (imSubtype == null) {
            kbId = null;
        } else {

            final String subtypeKbId = imSubtype.getExtraValueOf(SUBTYPE_STRING_ID_EXTRA_FIELD);
            kbId = ((App) getApplication()).getEditKbRepo().kbExists(subtypeKbId) ?
                    subtypeKbId : null;
        }
        this.kbId.setValue(Optional.ofNullable(kbId));
        kbKeys.observe(this, kbView::setContents);
        /*TODO if chosen kb is deleted*/
        return kbView;
    }

    @Override
    public void onWindowShown() {
        lifecycle.markState(Lifecycle.State.RESUMED);
        super.onWindowShown();
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        lifecycle.markState(Lifecycle.State.CREATED);
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
        kbId.setValue(Optional.of(newSubtype.getExtraValueOf(SUBTYPE_STRING_ID_EXTRA_FIELD)));
        Log.d("App", "SubtypeChanged=" + newSubtype);
    }

    @Override
    @NonNull
    public Lifecycle getLifecycle() {
        return lifecycle;
    }
}
