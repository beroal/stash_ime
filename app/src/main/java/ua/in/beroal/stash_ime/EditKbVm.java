package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import java8.util.Objects;
import java8.util.Optional;
import ua.in.beroal.java.NoMatchingConstant;
import ua.in.beroal.util.Unicode;

import static android.arch.lifecycle.Transformations.map;
import static android.arch.lifecycle.Transformations.switchMap;
import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Android.clipboardToChar;
import static ua.in.beroal.util.Android.map2;

public class EditKbVm extends AndroidViewModel {
    private static final String CHOSEN_KB_ID_FIELD = "chosen_kb_id";
    private static final String EDIT_MODE_FIELD = "edit_mode";
    private static final String EDIT_MODE_LINE_OP_FIELD = "edit_mode_line_op";
    private static final String EDIT_MODE_LINE_COORD_FIELD = "edit_mode_line_coord";
    private static final String IS_INSERT_KB_FORM_SHOWN_FIELD = "is_insert_kb_form_shown";
    private final MutableLiveData<EditKbMode> editMode = new MutableLiveData<>();
    private boolean initialized = false;
    @NonNull
    private Optional<String> chosenKbIdI;
    private MutableLiveData<Optional<String>> chosenKbId = new MutableLiveData<>();
    private EditKbMode editModeI;
    private MutableLiveData<Boolean> editModeInsertRow = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteRow = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeInsertColumn = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteColumn = new MutableLiveData<>();
    private LiveData<Pair<Integer, Iterable<? extends CharSequence>>> kbListSel;
    private MutableLiveData<Boolean> isInsertKbFormShown = new MutableLiveData<>();
    private MutableLiveData<Optional<KbKeys>> emptyKb = new MutableLiveData<>();
    private LiveData<KbViewState> kb;

    public EditKbVm(Application app) {
        super(app);
        emptyKb.setValue(Optional.empty());
        kbListSel = map(getRepo().getKbList(),
                kbList -> {
                    Optional<String> oldKbId = chosenKbIdI;
                    final int chosenKbIx = adaptChosenKbId(kbList);
                    if (!Objects.equals(chosenKbIdI, oldKbId)) {
                        chosenKbSetLd();
                    }
                    return new Pair<>(chosenKbIx, kbList);
                });
        LiveData<Optional<KbKeys>> kbKeysLiveData = switchMap(chosenKbId,
                kbId -> kbId.isEmpty() ? emptyKb
                        : getRepo().getKeysOptional(kbId.orElseThrow()));
        kb = map2(editMode, kbKeysLiveData, KbViewState::new);
    }

    /**
     * Changes {@link #chosenKbIdI} such that it is correct with respect to {@code kbList}.
     *
     * @return the index of {@link #chosenKbIdI}
     */
    private int adaptChosenKbId(@NonNull List<String> kbList) {
        final int i2;
        if (chosenKbIdI.isEmpty()) {
            i2 = kbList.size() == 0 ? -1 : 0;
        } else {
            final int i = Collections.binarySearch(kbList, chosenKbIdI.orElseThrow());
            if (i >= 0) {
                i2 = i;
            } else {
                final int i1 = -(i + 1);
                i2 = i1 == kbList.size()
                        ? kbList.size() == 0 ? -1 : kbList.size() - 1
                        : i1;
            }
        }
        chosenKbIdI = i2 == -1 ? Optional.empty() : Optional.of(kbList.get(i2));
        return i2;
    }

    @NonNull
    private EditKbRepo getRepo() {
        return ((App) getApplication()).getEditKbRepo();
    }

    private void writeEditModeToBundle(@NonNull Bundle outState) {
        final int editModeTag;
        if (editModeI instanceof EditKbModeKey) {
            editModeTag = 0;
        } else {
            if (editModeI instanceof EditKbModeLine) {
                editModeTag = 1;
            } else {
                throw new NoMatchingConstant();
            }
        }
        outState.putInt(EDIT_MODE_FIELD, editModeTag);
        if (editModeI instanceof EditKbModeLine) {
            outState.putInt(EDIT_MODE_LINE_OP_FIELD,
                    ((EditKbModeLine) editModeI).getOp().ordinal());
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putString(CHOSEN_KB_ID_FIELD, chosenKbIdI.orElse(null));
        writeEditModeToBundle(outState);
        outState.putBoolean(IS_INSERT_KB_FORM_SHOWN_FIELD, isInsertKbFormShown.getValue());
    }

    private void readEditModeFromBundle(@NonNull Bundle inState) {
        switch (inState.getInt(EDIT_MODE_FIELD)) {
            case 0:
                editModeI = new EditKbModeKey();
                break;
            case 1:
                editModeI = new EditKbModeLine(
                        EditKbModeLine.Op.values()[inState.getInt(EDIT_MODE_LINE_OP_FIELD)],
                        EditKbModeLine.Coord.values()[inState.getInt(EDIT_MODE_LINE_COORD_FIELD)]);
                break;
            default:
                throw new NoMatchingConstant();
        }
    }

    /**
     * This method must be called before calling other methods except {@link LiveData} getters.
     */
    public void restoreInstanceState(@Nullable Bundle inState) {
        if (!initialized) {
            final boolean isInsertKbFormShownValue;
            if (inState == null) {
                chosenKbIdI = Optional.empty();
                editModeI = new EditKbModeKey();
                isInsertKbFormShownValue = false;
            } else {
                chosenKbIdI = Optional.ofNullable(inState.getString(CHOSEN_KB_ID_FIELD));
                adaptChosenKbId(getRepo().getKbList().getValue());
                readEditModeFromBundle(inState);
                isInsertKbFormShownValue = inState.getBoolean(IS_INSERT_KB_FORM_SHOWN_FIELD);
            }
            chosenKbSetLd();
            isInsertKbFormShown.setValue(isInsertKbFormShownValue);
            editModeSetLdAll();
            initialized = true;
        }
    }

    @NonNull
    public LiveData<Pair<Integer, Iterable<? extends CharSequence>>> getKbList() {
        return kbListSel;
    }

    public void insertKbCreate(@NonNull String kbId) {
        try {
            if (!getRepo().insertKbCreate(kbId)) {
                Log.e("App", "A keyboard with this name exists.");
            } else {
                chosenKbIdI = Optional.of(kbId);
                getRepo().kbListSetLdPublic();
            }
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void insertKbImport(@NonNull String kbId, @NonNull Uri uri) {
        try {
            if (!getRepo().insertKbImport(kbId, uri)) {
                Log.e("App", "A keyboard with this name exists.");
            } else {
                chosenKbIdI = Optional.of(kbId);
                getRepo().kbListSetLdPublic();
            }
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void deleteKb() {
        chosenKbIdI.ifPresent(a -> {
            getRepo().deleteKb(a);
            getRepo().kbListSetLdPublic();
        });
    }

    @NonNull
    public LiveData<Boolean> getIsInsertKbFormShown() {
        return isInsertKbFormShown;
    }

    public void flipInsertKbForm() {
        isInsertKbFormShown.setValue(!isInsertKbFormShown.getValue());
    }

    public void hideInsertKbForm() {
        isInsertKbFormShown.setValue(false);
    }

    public void exportKb(@NonNull Uri uri) {
        chosenKbIdI.ifPresent(kbId -> {
            try {
                getRepo().exportKb(kbId, uri);
            } catch (FileNotFoundException e) {
                Log.e("App", "I/O error", e);
            } catch (IOException e) {
                Log.e("App", "I/O error", e);
            }
        });
    }

    private void chosenKbSetLd() {
        chosenKbId.setValue(chosenKbIdI);
    }

    public void setChosenKb(int ix) {
        chosenKbIdI = ix == -1 ? Optional.empty() : Optional.of(getRepo().kbIxToId(ix));
        chosenKbSetLd();
    }

    @NonNull
    private MutableLiveData<Boolean> getEditModeMutable(@NonNull EditKbModeLine a) {
        switch (a.getOp()) {
            case DELETE:
                switch (a.getCoord()) {
                    case ROW:
                        return editModeDeleteRow;
                    case COLUMN:
                        return editModeDeleteColumn;
                    default:
                        throw new NoMatchingConstant();
                }
            case INSERT:
                switch (a.getCoord()) {
                    case ROW:
                        return editModeInsertRow;
                    case COLUMN:
                        return editModeInsertColumn;
                    default:
                        throw new NoMatchingConstant();
                }
            default:
                throw new NoMatchingConstant();
        }
    }

    @NonNull
    public LiveData<Boolean> getEditMode(@NonNull EditKbModeLine a) {
        return getEditModeMutable(a);
    }

    private void editModeSetLd(@NonNull EditKbModeLine a) {
        getEditModeMutable(a).setValue(editModeI.equals(a));
    }

    private void editModeSetLdAll() {
        editModeSetLd(new EditKbModeLine(EditKbModeLine.Op.INSERT, EditKbModeLine.Coord.ROW));
        editModeSetLd(new EditKbModeLine(EditKbModeLine.Op.INSERT, EditKbModeLine.Coord.COLUMN));
        editModeSetLd(new EditKbModeLine(EditKbModeLine.Op.DELETE, EditKbModeLine.Coord.ROW));
        editModeSetLd(new EditKbModeLine(EditKbModeLine.Op.DELETE, EditKbModeLine.Coord.COLUMN));
        editMode.setValue(editModeI);
    }

    public void flipEditModeLine(@NonNull EditKbModeLine a) {
        editModeI = editModeI instanceof EditKbModeLine && editModeI.equals(a) ?
                new EditKbModeKey() : a;
        editModeSetLdAll();
    }

    /**
     * A keyboard must be chosen.
     */
    public void editModeDoOp(int i) {
        try {
            getRepo().editLineDoOp(chosenKbIdI.orElseThrow(), (EditKbModeLine) editModeI, i);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
        editModeI = new EditKbModeKey();
        editModeSetLdAll();
    }

    @NonNull
    public LiveData<KbViewState> getKb() {
        return kb;
    }

    /**
     * A keyboard must be chosen.
     */
    public void copyKey(@NonNull Pair<Integer, Integer> pos) {
        final int char1 = getRepo().getKey(chosenKbIdI.orElseThrow(), pos);
        if (char1 != Unicode.NO_CHAR) {
            ((App) getApplication()).getCharClipboardRepo().get().insertItem(char1);
            charToClipboard(getApplication().getApplicationContext(), char1);
        }
    }

    /**
     * Puts {@code char1} into the keyboard's key at {@code pos}. A keyboard must be chosen.
     */
    public void putKey(@NonNull Pair<Integer, Integer> pos, int char1) {
        try {
            getRepo().putKey(chosenKbIdI.orElseThrow(), pos, char1);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    /**
     * Executes {@code putKey(pos, char1)}
     * if {@code char1} is not {@link ua.in.beroal.util.Unicode#NO_CHAR}.
     * A keyboard must be chosen.
     */
    public void putKeyIfFilled(@NonNull Pair<Integer, Integer> pos, int char1) {
        if (char1 != Unicode.NO_CHAR) {
            try {
                getRepo().putKey(chosenKbIdI.orElseThrow(), pos, char1);
            } catch (IOException e) {
                Log.e("App", "I/O error", e);
            }
        }
    }

    public void clearKey(@NonNull Pair<Integer, Integer> pos) {
        putKey(pos, Unicode.NO_CHAR);
    }

    public void pasteKey(@NonNull Pair<Integer, Integer> pos) {
        putKeyIfFilled(pos, clipboardToChar(getApplication().getApplicationContext()));
    }

    @NonNull
    public CharSequence getExportKbFileName() {
        return (chosenKbIdI.orElse("keyboard")) + ".json";
    }
}
