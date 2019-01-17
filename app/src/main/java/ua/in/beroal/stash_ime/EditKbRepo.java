package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import java8.util.Optional;
import java8.util.function.Consumer;

import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Android.clipboardToChar;

public class EditKbRepo {
    private final TreeMap<String, KbRepo> kbFamily;
    private List<String> kbNameList;
    private Context context;
    private Consumer<List<String>> sendKbListToOs;
    private MutableLiveData<List<String>> kbListLiveData = new MutableLiveData<>();


    private void ensureKbNameList() {
        if (kbNameList == null) {
            kbNameList = new ArrayList<>(kbFamily.keySet());
        }
    }

    private EditKbRepo(Context context, TreeMap<String, KbRepo> kbFamily,
                       Consumer<List<String>> sendKbListToOs) {
        this.context = context;
        this.kbFamily = kbFamily;
        this.sendKbListToOs = sendKbListToOs;
        liveDataSetValue();
    }

    public static Pair<EditKbRepo, IOException> create(
            Context context, Consumer<List<String>> sendKbListToOs) {
        final TreeMap<String, KbRepo> kbFamily = new TreeMap<>();
        IOException e = null;
        for (File kbFile : context.getDir("kb", Context.MODE_PRIVATE).listFiles()) {
            try {
                final KbRepo kbRepo = new KbRepo(kbFile);
                kbFamily.put(kbFile.getName(), kbRepo);
            } catch (IOException e1) {
                e = e1;
            }
        }
        return new Pair<>(new EditKbRepo(context, kbFamily, sendKbListToOs), e);
    }

    private void liveDataSetValue() {
        ensureKbNameList();
        sendKbListToOs.accept(kbNameList);
        kbListLiveData.setValue(kbNameList);
    }

    public LiveData<List<String>> getKbListLiveData() {
        return kbListLiveData;
    }

    public boolean insertKb(String kbId) throws IOException {
        if (kbFamily.containsKey(kbId)) {
            return false;
        } else {
            kbFamily.put(kbId, new KbRepo(kbId, new File(
                    context.getDir("kb", Context.MODE_PRIVATE), kbId)));
            kbNameList = null;
            liveDataSetValue();
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
            liveDataSetValue();
            return true;
        }
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

    public LiveData<KbKeys> getKeysLiveData(String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeysLiveData();
        }
    }

    public LiveData<Optional<KbKeys>> getKeysOptionalLiveData(String kbId) {
        KbRepo kbRepo = kbFamily.get(kbId);
        if (kbRepo == null) {
            throw new IllegalArgumentException();
        } else {
            return kbRepo.getKeysOptionalLiveData();
        }
    }

    public int kbListSize() {
        return kbFamily.size();
    }

    public String kbIxToId(int ix) {
        ensureKbNameList();
        return kbNameList.get(ix);
    }

    public List<String> getKbList() {
        ensureKbNameList();
        return kbNameList;
    }

    public boolean kbExists(String kbId) {
        return kbFamily.containsKey(kbId);
    }
}