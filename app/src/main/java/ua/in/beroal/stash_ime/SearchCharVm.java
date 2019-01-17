package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.ibm.icu.text.UnicodeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import java8.util.function.Consumer;
import ua.in.beroal.java.FrequencyDivider;
import ua.in.beroal.util.Unicode;

import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Java.splitWords;
import static ua.in.beroal.util.ReactiveX.filterObservableContainsS;
import static ua.in.beroal.util.ReactiveX.observableToArrayList;

/**
 * A repository for this {@code ViewModel} is the Unicode Character Database which is constant
 * and is stored in the <a href="http://site.icu-project.org/home">ICU library</a>.
 * Character filtering is performed in a background thread via {@link CharListTask}.
 */
public class SearchCharVm extends AndroidViewModel {
    public static final String FILTER_WORDS_FIELD = "filter_words";
    public static final String FILTER_CPV_FIELD = "filter_cpv";
    private boolean initialized = false;
    private CharSequence wordsS;
    private ArrayList<Cpv> cpvFilterI;
    private MutableLiveData<List<String>> cpvFilter = new MutableLiveData<>();
    private MutableLiveData<List<CharForView>> charList = new MutableLiveData<>();
    private CharListTask charListTask;

    public SearchCharVm(@NonNull Application application) {
        super(application);
        Log.d("App", "SearchCharVm works on thread "
                + Thread.currentThread().getId() + ".");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCharListTask();
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putCharSequence(FILTER_WORDS_FIELD, wordsS);
        outState.putParcelableArrayList(FILTER_CPV_FIELD, cpvFilterI);
    }

    /**
     * This method must be called before calling other methods except {@link LiveData} getters.
     */
    public void restoreInstanceState(@Nullable Bundle inState) {
        if (!initialized) {
            cpvFilterI = inState == null
                    ? new ArrayList<>()
                    : inState.getParcelableArrayList(FILTER_CPV_FIELD);
            wordsS = inState == null ? ""
                    : inState.getCharSequence(FILTER_WORDS_FIELD);
            cpvFilterSetLd();
            executeCharListTask();
            initialized = true;
        }
    }

    public LiveData<List<String>> getCpvFilter() {
        return cpvFilter;
    }

    public LiveData<List<CharForView>> getCharList() {
        return charList;
    }

    public void setWordsS(CharSequence wordsS) {
        this.wordsS = wordsS.toString();
        executeCharListTask();
    }

    private void cpvFilterSetLd() {
        cpvFilter.setValue(observableToArrayList(
                Observable.fromIterable(cpvFilterI).map(CpvForView::cpvToString)));
    }

    public void deleteFilterItem(int i) {
        cpvFilterI.remove(i);
        cpvFilterSetLd();
        executeCharListTask();
    }

    public void insertFilterItem(Cpv a) {
        cpvFilterI.add(a);
        cpvFilterSetLd();
        executeCharListTask();
    }

    private void executeCharListTask() {
        cancelCharListTask();
        charListTask = new CharListTask(charList::setValue);
        charListTask.execute(new Pair<>(wordsS, new ArrayList<>(cpvFilterI)));

    }

    private void cancelCharListTask() {
        if (charListTask != null) {
            charListTask.cancel(true);
            charListTask = null;
        }
    }

    /**
     * @param char1 must differ from {@link ua.in.beroal.util.Unicode#NO_CHAR}
     */
    public void copyChar(int char1) {
        ((App) getApplication()).getCharClipboardRepo().get().insertItem(char1);
        charToClipboard(getApplication().getApplicationContext(), char1);
    }

    private static class CharListTask extends AsyncTask
            <Pair<CharSequence, List<Cpv>>, Void, List<CharForView>> {
        private Consumer<List<CharForView>> consumeRes;

        CharListTask(Consumer<List<CharForView>> consumeRes) {
            this.consumeRes = consumeRes;
            Log.d("App", "CharListTask was created.");
        }

        @Override
        protected List<CharForView> doInBackground(Pair<CharSequence, List<Cpv>>... a) {
            Log.d("App", "CharListTask has been started on thread "
                    + Thread.currentThread().getId() + ".");
            final Pair<CharSequence, List<Cpv>> a0 = a[0];
            final UnicodeSet charSet = Unicode.standardCharSet();
            for (Cpv cpv : a0.second) {
                charSet.retainAll(new UnicodeSet().applyIntPropertyValue(
                        cpv.getPropertyId(), cpv.getValueId()));
            }
            final ArrayList<CharForView> charList = new ArrayList<>();
            final FrequencyDivider frequencyDivider = new FrequencyDivider(100);
            final Observable<Integer> chars = filterObservableContainsS(splitWords(a0.first),
                    char1 -> Collections.singletonList(Unicode.getCharName(char1)),
                    Unicode.setToObservable(charSet));
            final Iterable<CharForView> charForViews = chars
                    .map(char1 -> new CharForView(char1, Unicode.getCharName(char1)))
                    .blockingIterable();
            for (CharForView item : charForViews) {
                if (frequencyDivider.tick()) {
                    if (isCancelled()) {
                        Log.d("App", "CharListTask has been cancelled.");
                        return null;
                    }
                }
                charList.add(item);
            }
            Log.d("App", "CharListTask has been completed.");
            return charList;
        }

        @Override
        protected void onPostExecute(List<CharForView> charList) {
            consumeRes.accept(charList);
            Log.d("App", "CharListTask.onPostExecute");
        }
    }
}
