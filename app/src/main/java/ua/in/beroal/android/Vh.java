package ua.in.beroal.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class Vh<Item> extends RecyclerView.ViewHolder {
    /**
     * You will need a static method (let's call it {@code create})
     * which creates and returns a {@link Vh} for the following reason.
     * You must create (or inflate) a {@link View} before giving it to a constructor of a subclass.
     * You can't do this in the constructor
     * because {@code super(view)} is the first statement in the constructor.
     * You can create a {@link View} in {@code create}.
     */
    public Vh(@NonNull View view) {
        super(view);
    }

    public abstract void setContents(Item item);
}