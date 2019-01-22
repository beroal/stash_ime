package ua.in.beroal.stash_ime;

import android.support.annotation.Nullable;

/**
 * A mode for changing individual keys,
 * like pasting a character into a key or moving a character from a key to another key.
 * Immutable.
 */
public final class EditKbModeKey extends EditKbMode {
    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }
}
