package ua.in.beroal.stash_ime;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static ua.in.beroal.util.Android.charToClipboard;

public class SearchCharActivity extends AppCompatActivity {

    /*private CharClipboardFragment charClipboardFragment;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_char);
        /*charClipboardFragment = (CharClipboardFragment) getSupportFragmentManager()
                .findFragmentById(R.id.char_clipboard_fragment);*/
    }

    /*@Override
    protected void onDestroy() {
        charClipboardFragment = null;
        super.onDestroy();
    }*/

    /*@Override
    public void copy(int char1) {
        charToClipboard(getApplicationContext(), char1);
        charClipboardFragment.onInsertFirstChar(char1);
    }*/
}
