package ua.in.beroal.stash_ime;

import android.arch.lifecycle.MutableLiveData;
import android.support.v4.util.AtomicFile;
import android.support.v4.util.Pair;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import java8.util.Optional;

import static ua.in.beroal.util.Gson.readJsonFromAtomicFile;
import static ua.in.beroal.util.Gson.writeJsonToAtomicFile;

public class KbRepo {
    public static final Type KB_KEYS_TYPE = new TypeToken<KbKeys>() {
    }.getType();
    private String name;
    private AtomicFile file;
    private KbKeys keys;
    private MutableLiveData<KbKeys> keysLiveData = new MutableLiveData<>();
    private MutableLiveData<Optional<KbKeys>> keysOptionalLiveData = new MutableLiveData<>();

    public KbRepo(String kbId, File kbFile) throws IOException {
        name = kbId;
        keys = new KbKeys();
        file = new AtomicFile(kbFile);
        writeKbLive();
    }

    public MutableLiveData<KbKeys> getKeysLiveData() {
        return keysLiveData;
    }

    public MutableLiveData<Optional<KbKeys>> getKeysOptionalLiveData() {
        return keysOptionalLiveData;
    }

    public KbRepo(File kbFile) throws IOException {
        name = kbFile.getName();
        file = new AtomicFile(kbFile);
        keys = readJsonFromAtomicFile(file, KbRepo.KB_KEYS_TYPE);
        keysLiveDatasetValue();
    }

    void editLineDoOp(EditKbModeLine editMode, int i) throws IOException {
        keys.editLineDoOp(editMode, i);
        writeKbLive();
    }

    private void writeKbLive() throws IOException {
        writeJsonToAtomicFile(keys, KB_KEYS_TYPE, file);
        keysLiveDatasetValue();
    }

    private void keysLiveDatasetValue() {
        final KbKeys keys1 = keys.clone();
        keysLiveData.setValue(keys1);
        keysOptionalLiveData.setValue(Optional.of(keys1));
    }

    public String getName() {
        return name;
    }

    public AtomicFile getFile() {
        return file;
    }

    public List<List<Integer>> getKeys() {
        return keys.getKeys();
    }

    public int getColumnCount() {
        return keys.getColumnCount();
    }

    public void delete() {
        file.delete();
    }

    public int getKey(Pair<Integer, Integer> pos) {
        return keys.getKey(pos);
    }

    public void putKey(Pair<Integer, Integer> pos, int char1) throws IOException {
        keys.putKey(pos, char1);
        writeKbLive();
    }

}
