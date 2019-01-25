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

import java.io.IOException;

import ua.in.beroal.java.NoMatchingConstant;
import ua.in.beroal.util.Unicode;

import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Android.clipboardToChar;
import static ua.in.beroal.util.Android.map2;

public class EditKbVm extends AndroidViewModel {
    private static final String EDIT_MODE_FIELD = "edit_mode";
    private static final String EDIT_MODE_LINE_OP_FIELD = "edit_mode_line_op";
    private static final String EDIT_MODE_LINE_COORD_FIELD = "edit_mode_line_coord";
    private static final String IS_INSERT_KB_FORM_SHOWN_FIELD = "is_insert_kb_form_shown";
    private final MutableLiveData<EditKbMode> editMode = new MutableLiveData<>();
    private final LiveData<KbViewState> kb;
    private boolean initialized = false;
    private EditKbMode editModeI;
    private MutableLiveData<Boolean> editModeInsertRow = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteRow = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeInsertColumn = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteColumn = new MutableLiveData<>();
    private MutableLiveData<Boolean> isInsertKbFormShown = new MutableLiveData<>();

    public EditKbVm(Application app) {
        super(app);
        kb = map2(editMode, getRepo().getKbKeys(), KbViewState::new);
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
                editModeI = new EditKbModeKey();
                isInsertKbFormShownValue = false;
            } else {
                readEditModeFromBundle(inState);
                isInsertKbFormShownValue = inState.getBoolean(IS_INSERT_KB_FORM_SHOWN_FIELD);
            }
            isInsertKbFormShown.setValue(isInsertKbFormShownValue);
            editModeSetLdAll();
            initialized = true;
        }
    }

    @NonNull
    public LiveData<Pair<Integer, Iterable<? extends CharSequence>>> getKbListSel() {
        return getRepo().getKbListSel();
    }

    public void insertKbCreate(@NonNull String kbId) {
        try {
            if (!getRepo().insertKbCreate(kbId)) {
                Log.e("App", "A keyboard with this name exists.");
            }
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void insertKbImport(@NonNull String kbId, @NonNull Uri uri) {
        try {
            if (!getRepo().insertKbImport(kbId, uri)) {
                Log.e("App", "A keyboard with this name exists.");
            }
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void deleteKb() {
        getRepo().deleteChosenKb();
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
        try {
            getRepo().exportChosenKb(uri);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void chooseKb(int kbIx) {
        getRepo().chooseKbIx(kbIx);
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

    public void editModeDoOp(int i) {
        try {
            getRepo().editChosenKbLine((EditKbModeLine) editModeI, i);
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
        final int char1 = getRepo().getChosenKbKey(pos);
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
            getRepo().putChosenKbKey(pos, char1);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    /**
     * Executes {@code putChosenKbKey(pos, char1)}
     * if {@code char1} is not {@link ua.in.beroal.util.Unicode#NO_CHAR}.
     * A keyboard must be chosen.
     */
    public void putKeyIfFilled(@NonNull Pair<Integer, Integer> pos, int char1) {
        if (char1 != Unicode.NO_CHAR) {
            try {
                getRepo().putChosenKbKey(pos, char1);
            } catch (IOException e) {
                Log.e("App", "I/O error", e);
            }
        }
    }

    /**
     * A keyboard must be chosen.
     */
    public void clearKey(@NonNull Pair<Integer, Integer> pos) {
        putKey(pos, Unicode.NO_CHAR);
    }

    /**
     * A keyboard must be chosen.
     */
    public void pasteKey(@NonNull Pair<Integer, Integer> pos) {
        putKeyIfFilled(pos, clipboardToChar(getApplication().getApplicationContext()));
    }

    @NonNull
    public CharSequence getExportKbFileName() {
        return (getRepo().getChosenKbName().orElse("keyboard")) + ".json";
    }
}
