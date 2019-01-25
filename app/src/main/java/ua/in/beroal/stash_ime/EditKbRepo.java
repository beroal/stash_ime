package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import java8.util.Optional;
import java8.util.function.Consumer;

/**
 * Accesses and caches keyboards in non-volatile memory.
 */
public class EditKbRepo {
    public static final String KB_DIR = "kb";
    private final TreeMap<String, KbRepo> kbFamily;
    @Nullable
    private List<String> kbNameList;
    private Context context;
    private Consumer<List<String>> sendKbListToOs;
    private MutableLiveData<List<String>> kbList = new MutableLiveData<>();
    private boolean kbListChanged = false;

    /**
     * {@code sendKbListToOs} will be called after creation of {@link EditKbRepo}
     * and when its list of keyboards is changed.
     */
    public EditKbRepo(@NonNull Context context, @NonNull Consumer<List<String>> sendKbListToOs)
            throws IOException {
        this.context = context;
        this.sendKbListToOs = sendKbListToOs;
        kbFamily = new TreeMap<>();
        IOException e = null;
        for (File kbFile : context.getDir(KB_DIR, Context.MODE_PRIVATE).listFiles()) {
            try {
                final KbRepo kbRepo = new KbRepo(kbFile, true);
                kbFamily.put(kbFile.getName(), kbRepo);
            } catch (IOException e1) {
                e = e1;
            }
        }
        kbListSetLd();
        if (e != null) {
            throw e;
        }
    }

    private void ensureKbNameList() {
        if (kbNameList == null) {
            kbNameList = new ArrayList<>(kbFamily.keySet());
        }
    }

    private void kbListSetLd() {
        ensureKbNameList();
        sendKbListToOs.accept(kbNameList);
        kbList.setValue(kbNameList);
    }

    public void kbListSetLdPublic() {
        if (kbListChanged) {
            kbListSetLd();
            kbListChanged = false;
        }
    }

    @NonNull
    public LiveData<List<String>> getKbList() {
        return kbList;
    }

    private boolean insertKb(@NonNull String kbId, @NonNull SupplierE<KbRepo> kbRepo)
            throws IOException {
        if (kbFamily.containsKey(kbId)) {
            return false;
        } else {
            kbFamily.put(kbId, kbRepo.get());
            kbNameList = null;
            kbListChanged = true;
            return true;
        }
    }

    /**
     * Does not set {@link LiveData}.
     */
    public boolean insertKbCreate(@NonNull String kbId) throws IOException {
        return insertKb(kbId, () -> new KbRepo(getKbFile(kbId), false));
    }

    /**
     * Does not set {@link LiveData}.
     */
    public boolean insertKbImport(@NonNull String kbId, @NonNull Uri uri)
            throws IOException {
        final boolean r;
        try (ParcelFileDescriptor fd = context.getContentResolver()
                .openFileDescriptor(uri, "r")) {
            r = insertKb(kbId, () -> new KbRepo(getKbFile(kbId), fd));
        }
        return r;
    }

    /**
     * Does not set {@link LiveData}.
     */
    public boolean deleteKb(@NonNull String kbId) {
        if (!kbFamily.containsKey(kbId)) {
            return false;
        } else {
            kbFamily.get(kbId).delete();
            kbFamily.remove(kbId);
            kbNameList = null;
            kbListChanged = true;
            return true;
        }
    }

    public boolean exportKb(@NonNull String kbId, @NonNull Uri uri)
            throws IOException {
        if (!kbFamily.containsKey(kbId)) {
            return false;
        } else {
            try (ParcelFileDescriptor fd = context.getContentResolver()
                    .openFileDescriptor(uri, "w")) {
                kbFamily.get(kbId).export(fd);
            }
            return true;
        }
    }

    @NonNull
    private File getKbFile(@NonNull String kbId) {
        return new File(context.getDir(KB_DIR, Context.MODE_PRIVATE), kbId);
    }

    @NonNull
    public LiveData<KbKeys> getKeys(@NonNull String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeys();
        }
    }

    @NonNull
    public LiveData<Optional<KbKeys>> getKeysOptional(@NonNull String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeysOptional();
        }
    }

    @NonNull
    public String kbIxToId(int ix) {
        ensureKbNameList();
        return kbNameList.get(ix);
    }

    public boolean kbExists(@NonNull String kbId) {
        return kbFamily.containsKey(kbId);
    }

    public int getKey(@NonNull String kbId, @NonNull Pair<Integer, Integer> pos) {
        return kbFamily.get(kbId).getKey(pos);
    }

    public void putKey(@NonNull String kbId, @NonNull Pair<Integer, Integer> pos, int char1)
            throws IOException {
        kbFamily.get(kbId).putKey(pos, char1);
    }

    public void editLineDoOp(@NonNull String kbId, @NonNull EditKbModeLine editMode, int i)
            throws IOException {
        kbFamily.get(kbId).editLineDoOp(editMode, i);
    }

    private interface SupplierE<T> {
        T get() throws IOException;
    }
}