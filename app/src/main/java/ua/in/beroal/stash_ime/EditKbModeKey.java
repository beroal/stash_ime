package ua.in.beroal.stash_ime;

import android.support.annotation.Nullable;

public class EditKbModeKey extends EditKbMode implements Cloneable {
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public EditKbModeKey clone() {
        return (EditKbModeKey) super.clone();
    }
}
