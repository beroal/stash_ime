package ua.in.beroal.stash_ime;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java8.util.Optional;

import static ua.in.beroal.util.Unicode.codePointToString;

public class KbView extends FrameLayout {
    private KeyboardView.OnKeyboardActionListener mListener;

    public KbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

    }
    public void setOnKeyboardActionListener (KeyboardView.OnKeyboardActionListener listener) {
        mListener = listener;
    }
    public void setContents(Optional<KbKeys> keysOptional) {
        Log.d("App", "KbView.setContents");
        if (getChildCount() != 0) {
            removeViewAt(0);
        }
        if (keysOptional.isEmpty()) {
            TextView emptyV = new TextView(getContext());
            emptyV.setText("no keyboard");
            addView(emptyV);
        } else {
            final KbKeys keys = keysOptional.orElseThrow();
            GridLayout gridV = new GridLayout(getContext());
            gridV.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            gridV.setBackgroundColor(0xFFCCCCCC);
            gridV.setColumnCount(keys.getColumnCount());
            int i = 0;
            for (Iterable<Integer> row : keys.getKeys()) {
                int j = 0;
                for (int char1 : row) {
                    final TextView keyV = new TextView(getContext());
                    keyV.setTextSize(22);
                    keyV.setGravity(Gravity.CENTER);
                    final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                            GridLayout.spec(i, GridLayout.FILL),
                            GridLayout.spec(j, GridLayout.FILL, 1F));
                    keyV.setLayoutParams(layoutParams);
                    if (char1 != -1) {
                        keyV.setText(codePointToString(char1));
                        keyV.setOnClickListener(v -> {
                            if (this.mListener != null) {
                                mListener.onPress(char1);
                                mListener.onKey(char1, new int[] {char1});
                                mListener.onRelease(char1);
                            }
                        });
                    }

                    gridV.addView(keyV);
                    j++;
                }
                i++;
            }
            addView(gridV);
        }

    }
}