package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import java8.util.Optional;
import java8.util.function.Consumer;

import static android.arch.lifecycle.Transformations.switchMap;

/**
 * Accesses and caches keyboards in non-volatile memory.
 */
public class EditKbRepo {
    public static final String KB_DIR = "kb";
    public static final String CHOSEN_KB_PREF_FIELD = "chosen_kb";
    private final TreeMap<String, KbRepo> kbFamily = new TreeMap<>();
    private final Context context;
    private final Consumer<List<String>> sendKbListToOs;
    private final MutableLiveData<Optional<String>> chosenKbId = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Iterable<? extends CharSequence>>> kbListSel =
            new MutableLiveData<>();
    private final LiveData<Optional<KbKeys>> kbKeys;
    private final MutableLiveData<Optional<KbKeys>> emptyKb = new MutableLiveData<>();
    @Nullable
    private List<String> kbNameList;

    /**
     * {@code sendKbListToOs} will be called after creation of {@link EditKbRepo}
     * and when its list of keyboards is changed.
     */
    public EditKbRepo(@NonNull Context context, @NonNull Consumer<List<String>> sendKbListToOs)
            throws IOException {
        this.context = context;
        this.sendKbListToOs = sendKbListToOs;
        emptyKb.setValue(Optional.empty());
        IOException e = null;
        for (File kbFile : context.getDir(KB_DIR, Context.MODE_PRIVATE).listFiles()) {
            try {
                final KbRepo kbRepo = new KbRepo(kbFile, true);
                kbFamily.put(kbFile.getName(), kbRepo);
            } catch (IOException e1) {
                e = e1;
            }
        }
        kbKeys = switchMap(chosenKbId,
                kbId -> kbId.isEmpty() ? emptyKb : getKeysOptional(kbId.orElseThrow()));
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(CHOSEN_KB_PREF_FIELD)) {
            chooseKbId(Optional.ofNullable(
                    sharedPreferences.getString(CHOSEN_KB_PREF_FIELD, null)));
        } else {
            chooseKbIx(kbFamily.size() == 0 ? -1 : 0);
        }
        if (e != null) {
            throw e;
        }
    }

    private static <T> int findNextByOrder(@NonNull List<? extends Comparable<? super T>> list,
                                           @NonNull T a) {
        final int i = Collections.binarySearch(list, a);
        final int i2;
        if (i >= 0) {
            i2 = i;
        } else {
            final int i1 = -(i + 1);
            i2 = i1 == list.size()
                    ? list.size() == 0 ? -1 : list.size() - 1
                    : i1;
        }
        return i2;
    }

    @NonNull
    private static <T extends Comparable<? super T>>
    Pair<Integer, Optional<T>> findNextValueByOrder(
            @NonNull List<? extends T> list, @NonNull Optional<? extends T> a) {
        final int i = a.isEmpty()
                ? -1
                : findNextByOrder(list, a.orElseThrow());
        return new Pair<>(i, i == -1 ? Optional.empty() : Optional.of(list.get(i)));
    }

    private void saveChosenKbId(@NonNull Optional<String> chosenKbIdI) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(CHOSEN_KB_PREF_FIELD, chosenKbIdI.orElse(null))
                .apply();
    }

    private void chooseKbId(@NonNull Optional<String> chosenKbIdI) {
        ensureKbNameList();
        final Pair<Integer, Optional<String>> a = findNextValueByOrder(kbNameList, chosenKbIdI);
        saveChosenKbId(a.second);
        chosenKbId.setValue(a.second);
        kbListSel.setValue(new Pair<>(a.first, kbNameList));
    }

    public void chooseKbIx(int kbIx) {
        ensureKbNameList();
        final Optional<String> chosenKbIdI = kbIx == -1
                ? Optional.empty()
                : Optional.of(kbNameList.get(kbIx));
        saveChosenKbId(chosenKbIdI);
        chosenKbId.setValue(chosenKbIdI);
        kbListSel.setValue(new Pair<>(kbIx, kbNameList));
    }

    private void ensureKbNameList() {
        if (kbNameList == null) {
            kbNameList = new ArrayList<>(kbFamily.keySet());
        }
    }

    @NonNull
    public LiveData<Pair<Integer, Iterable<? extends CharSequence>>> getKbListSel() {
        return kbListSel;
    }

    private boolean insertKb(@NonNull String kbId, @NonNull SupplierE<KbRepo> kbRepo)
            throws IOException {
        if (kbId.contains("/") || kbId.equals(".") || kbId.equals("..")
                || kbFamily.containsKey(kbId)) {
            return false;
        } else {
            kbFamily.put(kbId, kbRepo.get());
            kbNameList = null;
            chooseKbId(Optional.of(kbId));
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

    public void deleteChosenKb() {
        chosenKbId.getValue().ifPresent(kbId -> {
            kbFamily.get(kbId).delete();
            kbFamily.remove(kbId);
            kbNameList = null;
            chooseKbId(chosenKbId.getValue());
        });
    }

    public void exportChosenKb(@NonNull Uri uri) throws IOException {
        if (!chosenKbId.getValue().isEmpty()) {
            try (ParcelFileDescriptor fd = context.getContentResolver()
                    .openFileDescriptor(uri, "w")) {
                getKbRepo().export(fd);
            }
        }
    }

    @NonNull
    public Optional<String> getChosenKbName() {
        return chosenKbId.getValue();
    }

    @NonNull
    private File getKbFile(@NonNull String kbId) {
        return new File(context.getDir(KB_DIR, Context.MODE_PRIVATE), kbId);
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

    public boolean kbExists(@NonNull String kbId) {
        return kbFamily.containsKey(kbId);
    }

    /**
     * A keyboard must be chosen.
     */
    @NonNull
    private KbRepo getKbRepo() {
        return kbFamily.get(chosenKbId.getValue().orElseThrow());
    }

    /**
     * A keyboard must be chosen.
     */
    public int getChosenKbKey(@NonNull Pair<Integer, Integer> pos) {
        return getKbRepo().getKey(pos);
    }

    public void putChosenKbKey(@NonNull Pair<Integer, Integer> pos, int char1)
            throws IOException {
        getKbRepo().putKey(pos, char1);
    }

    public void editChosenKbLine(@NonNull EditKbModeLine editMode, int i)
            throws IOException {
        if (!chosenKbId.getValue().isEmpty()) {
            getKbRepo().editLineDoOp(editMode, i);
        }
    }

    @NonNull
    public LiveData<Optional<KbKeys>> getKbKeys() {
        return kbKeys;
    }

    private interface SupplierE<T> {
        T get() throws IOException;
    }
}