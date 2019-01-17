package ua.in.beroal.stash_ime;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java8.util.function.Consumer;
import ua.in.beroal.android.Vh;

import static ua.in.beroal.util.Android.inflateDoNotAttach;

class CpvVh extends Vh<Pair<String, Cpv>> {
    private TextView v;
    private Cpv cpv;

    CpvVh(@NonNull View v, Consumer<Cpv> onItemClick) {
        super(v);
        this.v = (TextView) v;
        this.v.setOnClickListener(v1 -> onItemClick.accept(cpv));
    }

    static CpvVh create(@NonNull ViewGroup parent, Consumer<Cpv> onItemClick) {
        final View v = inflateDoNotAttach(R.layout.cpv, parent);
        return new CpvVh(v, onItemClick);
    }

    @Override
    public void setContents(Pair<String, Cpv> a) {
        v.setText(a.first);
        this.cpv = a.second;
    }
}
