package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.File;
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

    public LiveData<List<String>> getKbList() {
        return kbList;
    }

    public boolean insertKb(String kbId) throws IOException {
        if (kbFamily.containsKey(kbId)) {
            return false;
        } else {
            final KbRepo kbRepo = new KbRepo(
                    new File(context.getDir(KB_DIR, Context.MODE_PRIVATE), kbId),
                    false);
            kbFamily.put(kbId, kbRepo);
            kbNameList = null;
            kbListSetLd();
            return true;
        }
    }

    public boolean deleteKb(String kbId) {
        if (!kbFamily.containsKey(kbId)) {
            return false;
        } else {
            kbFamily.get(kbId).delete();
            kbFamily.remove(kbId);
            kbNameList = null;
            kbListSetLd();
            return true;
        }
    }

    public LiveData<KbKeys> getKeys(String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeys();
        }
    }

    public LiveData<Optional<KbKeys>> getKeysOptional(String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeysOptional();
        }
    }

    public String kbIxToId(int ix) {
        ensureKbNameList();
        return kbNameList.get(ix);
    }

    public boolean kbExists(String kbId) {
        return kbFamily.containsKey(kbId);
    }

    public Integer getKey(String kbId, Pair<Integer, Integer> pos) {
        return kbFamily.get(kbId).getKey(pos);
    }

    public void putKey(String kbId, Pair<Integer, Integer> pos, int char1) throws IOException {
        kbFamily.get(kbId).putKey(pos, char1);
    }

    public void editLineDoOp(String kbId, EditKbModeLine editMode, int i) throws IOException {
        kbFamily.get(kbId).editLineDoOp(editMode, i);
    }
}