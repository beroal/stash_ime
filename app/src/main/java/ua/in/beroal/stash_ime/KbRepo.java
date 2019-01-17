package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.v4.util.AtomicFile;
import android.support.v4.util.Pair;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import java8.util.Optional;

import static ua.in.beroal.util.Gson.readJsonFromAtomicFile;
import static ua.in.beroal.util.Gson.writeJsonToAtomicFile;

/**
 * Accesses and caches a keyboard in non-volatile memory.
 */
public class KbRepo {
    public static final Type KB_KEYS_TYPE = new TypeToken<KbKeys>() {
    }.getType();
    private String name;
    private AtomicFile file;
    private KbKeys keysI;
    private MutableLiveData<KbKeys> keys = new MutableLiveData<>();
    private MutableLiveData<Optional<KbKeys>> keysOptional = new MutableLiveData<>();

    /**
     * @param read Read this keyboard from non-volatile memory.
     */
    public KbRepo(File kbFile, boolean read) throws IOException {
        name = kbFile.getName();
        file = new AtomicFile(kbFile);
        if (read) {
            keysI = readJsonFromAtomicFile(file, KbRepo.KB_KEYS_TYPE);
            keysSetLd();
        } else {
            keysI = new KbKeys();
            writeKbLd();
        }
    }

    private void writeKbLd() throws IOException {
        writeJsonToAtomicFile(keysI, KB_KEYS_TYPE, file);
        keysSetLd();
    }

    public String getName() {
        return name;
    }

    public LiveData<KbKeys> getKeys() {
        return keys;
    }

    public LiveData<Optional<KbKeys>> getKeysOptional() {
        return keysOptional;
    }

    public void editLineDoOp(EditKbModeLine editMode, int i) throws IOException {
        keysI.editLineDoOp(editMode, i);
        writeKbLd();
    }

    private void keysSetLd() {
        final KbKeys keys1 = keysI.clone();
        keys.setValue(keys1);
        keysOptional.setValue(Optional.of(keys1));
    }

    public void delete() {
        file.delete();
    }

    public int getKey(Pair<Integer, Integer> pos) {
        return keysI.getKey(pos);
    }

    public void putKey(Pair<Integer, Integer> pos, int char1) throws IOException {
        keysI.putKey(pos, char1);
        writeKbLd();
    }

}
