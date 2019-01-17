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

public class SearchCharVh extends Vh<CharRow> {
    private TextView image;
    private TextView code;
    private TextView name;
    private Button copy;
    private int char1;

    private SearchCharVh(@NonNull View v, Consumer<Integer> onItemClick) {
        super(v);
        image = (TextView) v.findViewById(R.id.char_row_image);
        code = (TextView) v.findViewById(R.id.char_row_code);
        name = (TextView) v.findViewById(R.id.char_row_name);
        copy = (Button) v.findViewById(R.id.char_row_copy);
        copy.setOnClickListener(v1 -> onItemClick.accept(char1));
    }

    public static SearchCharVh create(@NonNull ViewGroup parent, Consumer<Integer> onItemClick) {
        final View v = inflateDoNotAttach(R.layout.char_row, parent);
        return new SearchCharVh(v, onItemClick);
    }

    @Override
    public void setContents(CharRow charRow) {
        image.setText(Unicode.codePointToString(charRow.getCodePoint()));
        code.setText(String.format("%04X", charRow.getCodePoint()));
        name.setText(charRow.getName());
        char1 = charRow.getCodePoint();
    }
}
