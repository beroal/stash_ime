package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.AtomicFile;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static ua.in.beroal.util.Gson.readJsonFromAtomicFile;
import static ua.in.beroal.util.Gson.writeJsonToAtomicFile;

public class CharClipboardRepo {
    public static final int CHAR_CLIPBOARD_SIZE = 10;
    public static final Type CHAR_CLIPBOARD_TYPE = new TypeToken<ArrayList<Integer>>() {
    }.getType();
    private final Context context;
    private AtomicFile file;
    @NonNull
    private Deque<Integer> charClipboardI;
    private MutableLiveData<List<Integer>> charClipboard = new MutableLiveData<>();

    public CharClipboardRepo(@NonNull Context context) {
        this.context = context;
        this.file = new AtomicFile(new File(context.getFilesDir(), "clipboard.json"));
        initCharClipboard();
        setLd();
    }

    private void initCharClipboard() {
        try {
            final ArrayList<Integer> a = readJsonFromAtomicFile(file, CHAR_CLIPBOARD_TYPE);
            charClipboardI = new ArrayDeque<>(a.subList(0, Math.min(CHAR_CLIPBOARD_SIZE, a.size())));
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
            charClipboardI = new ArrayDeque<>(CHAR_CLIPBOARD_SIZE);
        }
    }

    /**
     * @return a result such as its observed values are not {@code null}
     */
    @NonNull
    public LiveData<List<Integer>> getCharClipboard() {
        return charClipboard;
    }

    private void write() {
        try {
            writeJsonToAtomicFile(charClipboardI, CHAR_CLIPBOARD_TYPE, file);
        } catch (IOException e) {
            Log.e("App", "I/O error", e);
        }
    }

    private void setLd() {
        charClipboard.setValue(new ArrayList<>(charClipboardI));
    }

    private void writeAndLd() {
        write();
        setLd();
    }

    /**
     * @param char1 must differ from {@link ua.in.beroal.util.Unicode#NO_CHAR}
     */
    public void insertItem(int char1) {
        if (CHAR_CLIPBOARD_SIZE == charClipboardI.size()) {
            charClipboardI.removeLast();
        }
        charClipboardI.addFirst(char1);
        writeAndLd();
    }
}
