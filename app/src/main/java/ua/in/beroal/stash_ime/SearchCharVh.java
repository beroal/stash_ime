package ua.in.beroal.stash_ime;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java8.util.function.Consumer;
import ua.in.beroal.android.Vh;
import ua.in.beroal.util.Unicode;

import static ua.in.beroal.util.Android.inflateDoNotAttach;

public class SearchCharVh extends Vh<CharForView> {
    private TextView image;
    private TextView codePoint;
    private TextView name;
    private int char1;

    public SearchCharVh(@NonNull View view, @NonNull Consumer<Integer> onItemClick) {
        super(view);
        image = (TextView) view.findViewById(R.id.char_row_image);
        codePoint = (TextView) view.findViewById(R.id.char_row_code);
        name = (TextView) view.findViewById(R.id.char_row_name);
        ((Button) view.findViewById(R.id.char_row_copy))
                .setOnClickListener(v1 -> onItemClick.accept(char1));
    }

    public static SearchCharVh create(@NonNull ViewGroup parent,
                                      @NonNull Consumer<Integer> onItemClick) {
        final View view = inflateDoNotAttach(R.layout.char_row, parent);
        return new SearchCharVh(view, onItemClick);
    }

    @Override
    public void setContents(CharForView charForView) {
        image.setText(Unicode.codePointToString(charForView.getCodePoint()));
        codePoint.setText(String.format("%04X", charForView.getCodePoint()));
        name.setText(charForView.getName());
        char1 = charForView.getCodePoint();
    }
}
