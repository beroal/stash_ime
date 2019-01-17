package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import ua.in.beroal.android.ClickPointTextView;
import ua.in.beroal.util.Unicode;

public class CharClipboardFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FrameLayout frameV = new FrameLayout(getContext());
        frameV.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final CharClipboardRepo charClipboardRepo = getApp().getCharClipboardRepo().get();
        charClipboardRepo.getCharClipboardLiveData().observe(this, chars -> {
            if (frameV.getChildCount() != 0) {
                frameV.removeViewAt(0);
            }
            final LinearLayout listV = new LinearLayout(getContext());
            listV.setOrientation(LinearLayout.HORIZONTAL);
            listV.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int i = 0;
            for (Integer char1 : chars) {
                final View item = getLayoutInflater().inflate(
                        R.layout.char_image, listV, false);
                final ClickPointTextView charImageV = (ClickPointTextView) item.findViewById(R.id.char_image);
                charImageV.setText(Unicode.codePointToString(char1));
                charImageV.setTag(i);
                charImageV.setOnClickPointListener((v, x, y) -> charClipboardRepo
                        .copyItemToClipboard((Integer) v.getTag()));
                charImageV.setOnDragFromListener(v -> v.startDragAndDrop(
                        ClipData.newPlainText("", Unicode.codePointToString(char1)),
                        new View.DragShadowBuilder(v),
                        null,
                        0));
                listV.addView(item);
                i++;
            }
            frameV.addView(listV);
        });
        return frameV;
    }

    private App getApp() {
        return (App) ((Activity) getContext()).getApplication();
    }

    /*public void onInsertFirstChar(int char1) {
        getApp().charClipboardRepo.get().insertFirstItem(char1);
    }*/
}
