package ua.in.beroal.stash_ime;

import java8.util.Optional;

public class KbViewState {
    private EditKbMode editMode;
    private Optional<KbKeys> keys;

    public KbViewState(EditKbMode editMode, Optional<KbKeys> keys) {
        this.editMode = editMode;
        this.keys = keys;
    }

    public EditKbMode getEditMode() {
        return editMode;
    }

    public Optional<KbKeys> getKeys() {
        return keys;
    }
}
