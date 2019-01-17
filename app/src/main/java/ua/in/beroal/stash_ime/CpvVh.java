package ua.in.beroal.stash_ime;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java8.util.function.Consumer;
import ua.in.beroal.android.Vh;

import static ua.in.beroal.util.Android.inflateDoNotAttach;

class CpvVh extends Vh<CpvForView> {
    private TextView view;
    private Cpv cpv;

    public CpvVh(@NonNull View view, @NonNull Consumer<Cpv> onItemClick) {
        super(view);
        this.view = (TextView) view;
        this.view.setOnClickListener(view1 -> onItemClick.accept(cpv));
    }

    public static CpvVh create(@NonNull ViewGroup parent, @NonNull Consumer<Cpv> onItemClick) {
        final View view = inflateDoNotAttach(R.layout.cpv, parent);
        return new CpvVh(view, onItemClick);
    }

    @Override
    public void setContents(@NonNull CpvForView a) {
        view.setText(a.getName());
        this.cpv = a.getCpv();
    }
}
