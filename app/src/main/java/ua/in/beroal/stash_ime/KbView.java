package ua.in.beroal.stash_ime;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java8.util.Optional;

import static ua.in.beroal.util.Unicode.codePointToString;

public class KbView extends FrameLayout {
    @Nullable
    private KeyboardView.OnKeyboardActionListener mListener;

    public KbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public KbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KbView(Context context) {
        this(context, null);
    }

    public void setOnKeyboardActionListener(KeyboardView.OnKeyboardActionListener listener) {
        mListener = listener;
    }

    @NonNull
    private TextView createKeyView(int rowI, int columnI, int char1) {
        final TextView keyView = new TextView(getContext());
        keyView.setTextSize(22);
        keyView.setGravity(Gravity.CENTER);
        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                GridLayout.spec(rowI, GridLayout.FILL),
                GridLayout.spec(columnI, GridLayout.FILL, 1F));
        keyView.setLayoutParams(layoutParams);
        if (char1 != -1) {
            keyView.setText(codePointToString(char1));
            keyView.setOnClickListener(v -> {
                if (this.mListener != null) {
                    mListener.onPress(char1);
                    mListener.onKey(char1, new int[]{char1});
                    mListener.onRelease(char1);
                }
            });
        }
        return keyView;
    }

    public void setContents(@NonNull Optional<KbKeys> keysOptional) {
        Log.d("App", "KbView.setContents");
        if (getChildCount() != 0) {
            removeViewAt(0);
        }
        if (keysOptional.isEmpty()) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("no keyboard");
            addView(emptyView);
        } else {
            final KbKeys keys = keysOptional.orElseThrow();
            GridLayout gridV = new GridLayout(getContext());
            gridV.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            gridV.setBackgroundColor(0xFFCCCCCC);
            gridV.setColumnCount(keys.getColumnCount());
            int rowI = 0;
            for (Iterable<Integer> row : keys.getKeys()) {
                int columnI = 0;
                for (int char1 : row) {
                    final TextView keyView = createKeyView(rowI, columnI, char1);
                    gridV.addView(keyView);
                    columnI++;
                }
                rowI++;
            }
            addView(gridV);
        }
    }
}