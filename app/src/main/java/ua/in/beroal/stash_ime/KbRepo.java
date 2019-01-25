package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.util.AtomicFile;
import android.support.v4.util.Pair;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import java8.util.Optional;

import static ua.in.beroal.util.Gson.readJsonFromAtomicFile;
import static ua.in.beroal.util.Gson.readJsonFromStream;
import static ua.in.beroal.util.Gson.writeJsonToAtomicFile;
import static ua.in.beroal.util.Gson.writeJsonToStream;

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
    public KbRepo(@NonNull File kbFile, boolean read) throws IOException {
        init(kbFile);
        if (read) {
            keysI = readJsonFromAtomicFile(file, KbRepo.KB_KEYS_TYPE);
            keysSetLd();
        } else {
            keysI = new KbKeys();
            writeKbLd();
        }
    }

    public KbRepo(@NonNull File kbFile, @NonNull ParcelFileDescriptor fd) throws IOException {
        init(kbFile);
        keysI = readJsonFromStream(new FileInputStream(fd.getFileDescriptor()), KbRepo.KB_KEYS_TYPE);
        writeKbLd();
    }

    private void init(@NonNull File kbFile) {
        name = kbFile.getName();
        file = new AtomicFile(kbFile);
    }

    private void writeKbLd() throws IOException {
        writeJsonToAtomicFile(keysI, KB_KEYS_TYPE, file);
        keysSetLd();
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public LiveData<KbKeys> getKeys() {
        return keys;
    }

    @NonNull
    public LiveData<Optional<KbKeys>> getKeysOptional() {
        return keysOptional;
    }

    public void editLineDoOp(@NonNull EditKbModeLine editMode, int i) throws IOException {
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

    public void export(@NonNull ParcelFileDescriptor uri) throws IOException {
        writeJsonToStream(keysI, KB_KEYS_TYPE, new FileOutputStream(uri.getFileDescriptor()));
    }

    public int getKey(@NonNull Pair<Integer, Integer> pos) {
        return keysI.getKey(pos);
    }

    public void putKey(@NonNull Pair<Integer, Integer> pos, int char1) throws IOException {
        keysI.putKey(pos, char1);
        writeKbLd();
    }
}
