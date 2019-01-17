package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;

import java8.util.Objects;
import java8.util.Optional;

import static android.arch.lifecycle.Transformations.map;
import static android.arch.lifecycle.Transformations.switchMap;
import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Android.clipboardToChar;
import static ua.in.beroal.util.Android.map2;

class EditKbVm extends AndroidViewModel {
    private static final String CHOSEN_KB_ID_FIELD = "chosen_kb_id";
    /*private static final String CHOSEN_KB_IX_FIELD = "chosen_kb_ix";*/
    private static final String EDIT_MODE_FIELD = "edit_mode";
    private static final String IS_INSERT_KB_FORM_SHOWN_FIELD = "is_insert_kb_form_shown";
    private final MutableLiveData<EditKbMode> editModeLiveData = new MutableLiveData<>();
    private String chosenKbId;
    private String insertChosenKbId;
    /*private int chosenKbIx;*/
    private MutableLiveData<String> kbIdLiveData = new MutableLiveData<>();
    private EditKbMode editMode;
    private MutableLiveData<Boolean> editModeInsertRowLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteRowLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeInsertColumnLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> editModeDeleteColumnLiveData = new MutableLiveData<>();
    private LiveData<Pair<Integer, Iterable<? extends CharSequence>>> kbListSelLiveData;
    private MutableLiveData<Boolean> isInsertKbFormShown = new MutableLiveData<>();
    private MutableLiveData<Optional<KbKeys>> emptyKbLiveData = new MutableLiveData<>();
    private LiveData<KbViewState> kbLiveData;

    public EditKbVm(Application app) {
        super(app);
        emptyKbLiveData.setValue(Optional.empty());
        kbListSelLiveData = map(getRepo().getKbListLiveData(),
                kbList -> {
                    String oldKbId = chosenKbId;
                    if (insertChosenKbId != null) {
                        chosenKbId = insertChosenKbId;
                        insertChosenKbId = null;
                    }
                    final int i2;
                    if (chosenKbId == null) {
                        i2 = kbList.size() == 0 ? -1 : 0;
                    } else {
                        final int i = Collections.binarySearch(kbList, chosenKbId);
                        if (i >= 0) {
                            i2 = i;
                        } else {
                            final int i1 = -(i + 1);
                            i2 = i1 == kbList.size() ?
                                    kbList.size() == 0 ? -1 : kbList.size() - 1
                                    : i1;
                        }
                        chosenKbId = i2 == -1 ? null : kbList.get(i2);
                        if (!Objects.equals(chosenKbId, oldKbId)) {
                            liveDataSetChosenKb();
                        }
                    }
                    return new Pair<>(i2, kbList);
                });
        LiveData<Optional<KbKeys>> kbKeysLiveData = switchMap(kbIdLiveData,
                kbId -> kbId == null ? emptyKbLiveData
                        : map(getRepo().getKeysLiveData(kbId), Optional::of));
        kbLiveData = map2(editModeLiveData, kbKeysLiveData, KbViewState::new);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CHOSEN_KB_ID_FIELD, chosenKbId);
        /*outState.putInt(CHOSEN_KB_IX_FIELD, chosenKbIx);*/
        /*TODO edit mode*/
        outState.putBoolean(IS_INSERT_KB_FORM_SHOWN_FIELD, isInsertKbFormShown.getValue());
    }

    public void restoreInstanceState(Bundle inState) {
        final boolean isInsertKbFormShownValue;
        if (inState == null) {
            chosenKbId = null;
            /*chosenKbIx = -1;*/
            /*TODO edit mode*/
            editMode = new EditKbModeKey();
            isInsertKbFormShownValue = false;

        } else {
            chosenKbId = inState.getString(CHOSEN_KB_ID_FIELD);
            /*chosenKbIx = inState.getInt(CHOSEN_KB_IX_FIELD);*/
            /*TODO edit mode*/
            editMode = new EditKbModeKey();
            isInsertKbFormShownValue = inState.getBoolean(IS_INSERT_KB_FORM_SHOWN_FIELD);

        }
        liveDataSetChosenKb();
        isInsertKbFormShown.setValue(isInsertKbFormShownValue);
        editModeLiveDataSetValueAll();

    }

    private void liveDataSetChosenKb() {
        kbIdLiveData.setValue(chosenKbId);
    }

    private EditKbRepo getRepo() {
        return ((App) getApplication()).getEditKbRepo();
    }

    public LiveData<KbViewState> getKbLiveData() {
        return kbLiveData;
    }

    public LiveData<Boolean> getIsInsertKbFormShown() {
        return isInsertKbFormShown;
    }

    public LiveData<Pair<Integer, Iterable<? extends CharSequence>>> getKbListLiveData() {
        return kbListSelLiveData;
    }

    public void clearKey(Pair<Integer, Integer> pos) {
        try {
            getRepo().putKey(chosenKbId, pos, -1);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    public void copyKey(Pair<Integer, Integer> pos) {
        final int char1 = getRepo().getKey(chosenKbId, pos);
        if (char1 != -1) {
            ((App) getApplication()).getCharClipboardRepo().get().insertFirstItem(char1);
            charToClipboard(getApplication().getApplicationContext(), char1);
        }
    }

    public void pasteKey(Pair<Integer, Integer> pos) {
        final int char1 = clipboardToChar(getApplication().getApplicationContext());
        if (char1 != -1) {
            try {
                getRepo().putKey(chosenKbId, pos, char1);
            } catch (IOException e) {
                Log.e("App", "I/O error", e);
            }
        }
    }
    /*public void keyToClipboard(String kbId, Pair<Integer, Integer> pos) {
        final Integer char1 = getKey(kbId, pos);
        if (char1 != -1) {
            charToClipboard(context, char1);
        }
    }

    public void clipboardToKey(String kbId, Pair<Integer, Integer> pos) throws IOException {
        final int char1 = clipboardToChar(context);
        if (char1 != -1) {
            kbFamily.get(kbId).putKey(pos, char1);
        }
    }



    public void clipboardToKey(Pair<Integer, Integer> pos) {
        try {
            getRepo().clipboardToKey(chosenKbId, pos);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }*/

    public void setChosenKb(int ix) {
        chosenKbId = ix == -1 ? null : getRepo().kbIxToId(ix);
        liveDataSetChosenKb();
    }

    private int findIx(Iterable<String> kbList) {
        int r = -1;
        int i = 0;
        for (String kbId : kbList) {
            if (chosenKbId.equals(kbId)) {
                r = i;
                break;
            }
            i++;
        }
        return r;
    }

    public void insertKb(String kbId) {
        insertChosenKbId = kbId;
        try {
            final boolean success;
            try {
                success = getRepo().insertKb(kbId);
            } finally {
                insertChosenKbId = null;
            }
            if (!success) {
                Log.e("Search", "A keyboard with this name exists.");
            }
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }

    }


    public void deleteKb() {
        getRepo().deleteKb(chosenKbId);
    }

    private int repoGetKey(Pair<Integer, Integer> pos) {
        return getRepo().getKey(chosenKbId, pos);
    }

    public void putKey(Pair<Integer, Integer> pos, int char1) {
        try {
            getRepo().putKey(chosenKbId, pos, char1);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    private void editModeLiveDataSetValue(EditKbModeLine a) {
        getEditModeMutableLiveData(a).setValue(editMode.equals(a));
    }

    private void editModeLiveDataSetValueAll() {
        editModeLiveDataSetValue(new EditKbModeLine(1, 0));
        editModeLiveDataSetValue(new EditKbModeLine(1, 1));
        editModeLiveDataSetValue(new EditKbModeLine(0, 0));
        editModeLiveDataSetValue(new EditKbModeLine(0, 1));
        editModeLiveData.setValue(editMode);
    }

    public void flipEditModeLine(EditKbModeLine a) {
        editMode = editMode instanceof EditKbModeLine && editMode.equals(a) ?
                new EditKbModeKey() : a;
        editModeLiveDataSetValueAll();
        liveDataSetChosenKb();
    }

    private MutableLiveData<Boolean> getEditModeMutableLiveData(EditKbModeLine a) {
        switch (a.getOp()) {
            case 0:
                switch (a.getCoord()) {
                    case 0:
                        return editModeDeleteRowLiveData;
                    case 1:
                        return editModeDeleteColumnLiveData;
                    default:
                        throw new IllegalArgumentException();
                }
            case 1:
                switch (a.getCoord()) {
                    case 0:
                        return editModeInsertRowLiveData;
                    case 1:
                        return editModeInsertColumnLiveData;
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    public LiveData<Boolean> getEditModeLiveData(EditKbModeLine a) {
        return getEditModeMutableLiveData(a);
    }

    public void editModeDoOp(int i) {
        try {
            getRepo().editLineDoOp(chosenKbId, (EditKbModeLine) editMode, i);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
        editMode = new EditKbModeKey();
        editModeLiveDataSetValueAll();
        liveDataSetChosenKb();
    }

    public void flipInsertKbForm() {
        isInsertKbFormShown.setValue(!isInsertKbFormShown.getValue());
    }

    public void hideInsertKbForm() {
        isInsertKbFormShown.setValue(false);
    }


}
