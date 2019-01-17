package ua.in.beroal.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import java8.util.function.Function;

public class ListAdapter<Item> extends RecyclerView.Adapter<Vh<Item>> {
    protected List<Item> data;
    private Function<ViewGroup, Vh<Item>> vhFactory;

    public ListAdapter() {
        this(Collections.emptyList());
    }

    public ListAdapter(List<Item> data) {
        this.data = data;
    }

    public void setVhFactory(Function<ViewGroup, Vh<Item>> vhFactory) {
        this.vhFactory = vhFactory;
    }

    public void setData(List<Item> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public Vh<Item> onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return vhFactory.apply(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull Vh<Item> vh, int i) {
        vh.setContents(data.get(i));
    }

}
