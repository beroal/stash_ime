package ua.in.beroal.stash_ime;

import android.support.annotation.Nullable;

public abstract class EditKbMode {
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public EditKbMode clone() {
        try {
            return (EditKbMode) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("TODO");
        }
    }
}
