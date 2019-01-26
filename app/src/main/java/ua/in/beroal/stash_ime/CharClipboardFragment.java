package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

import ua.in.beroal.android.ClickPointTextView;
import ua.in.beroal.util.Unicode;

import static ua.in.beroal.util.Android.charToClipboard;

/**
 * This fragment has no GUI state, so it does not access a {@code ViewModel},
 * it directly accesses the repository {@link CharClipboardRepo} instead.
 */
public class CharClipboardFragment extends Fragment {
    private static void onChanged(@NonNull Fragment fragment, @NonNull ViewGroup rootView,
                                  @Nullable List<Integer> chars) {
        if (chars != null) {
            if (rootView.getChildCount() != 0) {
                rootView.removeViewAt(0);
            }
            final LinearLayout listView = new LinearLayout(rootView.getContext());
            listView.setOrientation(LinearLayout.HORIZONTAL);
            listView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (Integer char1 : chars) {
                final View itemView = fragment.getLayoutInflater().inflate(
                        R.layout.char_token, listView, false);
                final ClickPointTextView charImageV =
                        (ClickPointTextView) itemView.findViewById(R.id.char_image);
                charImageV.setText(Unicode.codePointToString(char1));
                charImageV.setOnClickPointListener((v, x, y) -> charToClipboard(
                        fragment.getContext().getApplicationContext(), char1));
                charImageV.setOnDragStartedListener((v, x, y) -> v.startDragAndDrop(
                        ClipData.newPlainText("", Unicode.codePointToString(char1)),
                        new View.DragShadowBuilder(v),
                        null,
                        0));
                listView.addView(itemView);
            }
            rootView.addView(listView);
        }
    }

    private App getApp() {
        return (App) ((Activity) getContext()).getApplication();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FrameLayout rootView = new FrameLayout(getContext());
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getApp().getCharClipboardRepo().get().getCharClipboard()
                .observe(this, chars -> onChanged(this, rootView, chars));
        return rootView;
    }
}
