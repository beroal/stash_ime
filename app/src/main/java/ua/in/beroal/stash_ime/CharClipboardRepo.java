package ua.in.beroal.stash_ime;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.AtomicFile;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Gson.readJsonFromAtomicFile;
import static ua.in.beroal.util.Gson.writeJsonToAtomicFile;

public class CharClipboardRepo {
    public static final int CHAR_CLIPBOARD_SIZE = 10;
    public static final Type CHAR_CLIPBOARD_TYPE = new TypeToken<ArrayList<Integer>>() {}.getType();
    private final Context context;
    private AtomicFile file;
    private ArrayList<Integer> charClipboard; /* TODO replace with Deque */
    private MutableLiveData<List<Integer>> charClipboardLiveData = new MutableLiveData<>();

    public CharClipboardRepo(Context context) {
        this.context = context;
        this.file = new AtomicFile(new File(context.getFilesDir(), "clipboard.json"));
        initCharClipboard();
        liveDataSetValue();
    }

    private void initCharClipboard() {
        try {
            charClipboard = readJsonFromAtomicFile(file, CHAR_CLIPBOARD_TYPE);
        } catch (IOException e) {
            Log.e("Search", "I/O error", e);
            charClipboard = createEmptyCharClipboard();
        }
    }

    @NonNull
    public static ArrayList<Integer> createEmptyCharClipboard() {
        return new ArrayList<>(CHAR_CLIPBOARD_SIZE);
    }

    public MutableLiveData<List<Integer>> getCharClipboardLiveData() {
        return charClipboardLiveData;
    }

    private void liveDataSetValue() {
        charClipboardLiveData.setValue((List<Integer>) charClipboard.clone());
    }

    private void writeAndLiveData() {
        write();
        liveDataSetValue();
    }

    private void write() {
        try {
            writeJsonToAtomicFile(charClipboard, CHAR_CLIPBOARD_TYPE, file);
        } catch (IOException e) {
            Log.e("Search", "I/O error", e);
        }
    }

    void insertFirstItem(int item) {
        if (CHAR_CLIPBOARD_SIZE == charClipboard.size()) {
            charClipboard.remove(charClipboard.size() - 1);
        }
        charClipboard.add(0, item);
        writeAndLiveData();
    }

    void copyItemToClipboard(int ix) {
        charToClipboard(context, charClipboard.get(ix));
    }
}
