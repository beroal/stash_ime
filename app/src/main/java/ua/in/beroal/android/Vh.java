package ua.in.beroal.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class Vh<Item> extends RecyclerView.ViewHolder {
    public Vh(@NonNull View v) {
        super(v);
    }

    public abstract void setContents(Item item);
}