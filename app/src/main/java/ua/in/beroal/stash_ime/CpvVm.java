package ua.in.beroal.stash_ime;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ua.in.beroal.util.Java.splitWords;
import static ua.in.beroal.util.ReactiveX.observableToArrayList;

public class CpvVm extends ViewModel {
    public static final String WORDS_FIELD = "words";
    private boolean initialized = false;
    private CharSequence wordsS;
    private MutableLiveData<List<CpvForView>> cpvList = new MutableLiveData<>();

    @NonNull
    public static ArrayList<CpvForView> filteredCpvList(@NonNull CharSequence wordsS) {
        return observableToArrayList(Unicode.filteredCpvObservable(splitWords(wordsS))
                .map(CpvForView::new));
    }

    public MutableLiveData<List<CpvForView>> getCpvList() {
        return cpvList;
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putCharSequence(WORDS_FIELD, wordsS);
    }

    private void setLd() {
        cpvList.setValue(filteredCpvList(wordsS));
    }

    /**
     * This method must be called before calling other methods except {@link LiveData} getters.
     */
    public void restoreInstanceState(@Nullable Bundle inState) {
        if (!initialized) {
            wordsS = inState == null ? "" : inState.getCharSequence(WORDS_FIELD);
            setLd();
            initialized = true;
        }
    }

    public void setWordsS(@NonNull CharSequence wordsS) {
        this.wordsS = wordsS;
        setLd();
    }
}
