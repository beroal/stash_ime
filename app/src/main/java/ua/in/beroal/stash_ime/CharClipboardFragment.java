package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.arch.lifecycle.Observer;
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

/**
 * This fragment has no GUI state, so it does not access a {@code ViewModel},
 * it directly accesses the repository {@link CharClipboardRepo} instead.
 */
public class CharClipboardFragment extends Fragment {
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
                .observe(this, new Observer1(rootView));
        return rootView;
    }

    private class Observer1 implements Observer<List<Integer>> {
        private final ViewGroup rootView;
        private final CharClipboardRepo charClipboardRepo;

        public Observer1(ViewGroup rootView) {
            this.rootView = rootView;
            this.charClipboardRepo = getApp().getCharClipboardRepo().get();
        }

        @Override
        public void onChanged(@Nullable List<Integer> chars) {
            if (chars == null) {
                throw new IllegalArgumentException(
                        "CharClipboardRepo.getCharClipboard must not send null");
            } else {
                if (rootView.getChildCount() != 0) {
                    rootView.removeViewAt(0);
                }
                final LinearLayout listView = new LinearLayout(
                        CharClipboardFragment.this.getContext());
                listView.setOrientation(LinearLayout.HORIZONTAL);
                listView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int i = 0;
                for (Integer char1 : chars) {
                    final View itemView = getLayoutInflater().inflate(
                            R.layout.char_token, listView, false);
                    final ClickPointTextView charImageV =
                            (ClickPointTextView) itemView.findViewById(R.id.char_image);
                    charImageV.setText(Unicode.codePointToString(char1));
                    charImageV.setTag(i);
                    charImageV.setOnClickPointListener((v, x, y) -> charClipboardRepo
                            .itemToClipboard((Integer) v.getTag()));
                    charImageV.setOnDragStartedListener((v, x, y) -> v.startDragAndDrop(
                            ClipData.newPlainText("", Unicode.codePointToString(char1)),
                            new View.DragShadowBuilder(v),
                            null,
                            0));
                    listView.addView(itemView);
                    i++;
                }
                rootView.addView(listView);
            }
        }
    }
}
