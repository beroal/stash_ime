package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.ibm.icu.text.UnicodeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import java8.util.function.Consumer;

import static ua.in.beroal.util.Android.charToClipboard;
import static ua.in.beroal.util.Java.splitWords;
import static ua.in.beroal.util.ReactiveX.filterObservableContainsS;
import static ua.in.beroal.util.ReactiveX.observableToArrayList;
import static ua.in.beroal.util.Unicode.setToObservable;
import static ua.in.beroal.util.Unicode.standardCharSet;

public class SearchCharVm extends AndroidViewModel {
    private boolean initialized = false;
    private String wordsS;
    private ArrayList<Cpv> cpvFilter;
    private MutableLiveData<List<String>> cpvFilterLd = new MutableLiveData<>();
    private MutableLiveData<List<CharRow>> charListLd = new MutableLiveData<>();
    private CharListTask charListTask;

    public SearchCharVm(@NonNull Application application) {
        super(application);
        Log.d("App", "SearchCharVm works on thread "
                + Thread.currentThread().getId() + ".");
    }

    public LiveData<List<String>> getCpvFilterLd() {
        return cpvFilterLd;
    }

    public LiveData<List<CharRow>> getCharListLd() {
        return charListLd;
    }

    private void cpvFilterSetLd() {
        final ArrayList<String> cpvFilterForView = observableToArrayList(
                Observable.fromIterable(cpvFilter)
                        .map(ua.in.beroal.stash_ime.Unicode::cpvS));
        cpvFilterLd.setValue(cpvFilterForView);
    }

    private void executeCharListTask() {
        cancelCharListTask();
        charListTask = new CharListTask(charListLd::setValue);
        charListTask.execute(new Pair<>(new String(wordsS), new ArrayList<>(cpvFilter)));

    }

    private void cancelCharListTask() {
        if (charListTask != null) {
            charListTask.cancel(true);
        }
    }

    public void setWordsS(CharSequence wordsS) {
        this.wordsS = wordsS.toString();
        executeCharListTask();
    }

    public void deleteFilterItem(int i) {
        cpvFilter.remove(i);
        cpvFilterSetLd();
        executeCharListTask();
    }

    public void insertFilterItem(Cpv a) {
        cpvFilter.add(a);
        cpvFilterSetLd();
        executeCharListTask();
    }

    public void restoreInstanceState(Bundle inState) {
        if (!initialized) {
            cpvFilter = inState == null
                    ? new ArrayList<>()
                    : inState.getParcelableArrayList("filter_cvp_list");
            wordsS = inState == null ? ""
                    : inState.getString("filter_words");
            cpvFilterSetLd();
            executeCharListTask();
        }
    }

    public void saveInstanceState(Bundle outState) {
        outState.putString("filter_words", wordsS);
        outState.putParcelableArrayList("filter_cvp_list", cpvFilter);
    }

    public void copyChar(int char1) {
        if (char1 != -1) {
            ((App) getApplication()).getCharClipboardRepo().get().insertFirstItem(char1);
            charToClipboard(getApplication().getApplicationContext(), char1);
        }
    }

    private static class CharListTask extends AsyncTask<Pair<String, List<Cpv>>, Void, List<CharRow>> {
        private Consumer<List<CharRow>> consumeRes;

        public CharListTask(Consumer<List<CharRow>> consumeRes) {
            this.consumeRes = consumeRes;
            Log.d("App", "CharListTask was created.");
        }

        @Override
        protected List<CharRow> doInBackground(Pair<String, List<Cpv>>... a) {
            Log.d("App", "CharListTask has been started on thread "
                    + Thread.currentThread().getId() + ".");
            final Pair<String, List<Cpv>> a0 = a[0];
            String wordsS = a0.first;
            List<Cpv> cpvFilter = a0.second;
            final UnicodeSet charSet = standardCharSet();
            for (Cpv cpv : cpvFilter) {
                charSet.retainAll(new UnicodeSet().applyIntPropertyValue(
                        cpv.getPropertyId(), cpv.getValueId()));
            }
            final ArrayList<CharRow> charList = new ArrayList<>();
            final int ITERATION_COUNT = 100;
            int iterationIx = 0;
            boolean success = true;
            for (CharRow item
                    : filterObservableContainsS(splitWords(wordsS),
                    char1 -> Collections.singletonList(ua.in.beroal.util.Unicode.getCharName(char1)),
                    setToObservable(charSet))
                    .map(char1 -> new CharRow(char1, ua.in.beroal.util.Unicode.getCharName(char1)))
                    .blockingIterable()) {
                {
                    if (iterationIx == 0) {
                        iterationIx = ITERATION_COUNT;
                        if (isCancelled()) {
                            Log.d("App", "CharListTask has been cancelled.");
                            return null;
                        }
                    }
                    iterationIx--;
                }
                charList.add(item);
            }
            Log.d("App", "CharListTask has been completed.");
            return charList;
        }

        @Override
        protected void onPostExecute(List<CharRow> charList) {
            consumeRes.accept(charList);
            Log.d("App", "CharListTask.onPostExecute");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCharListTask();
        charListTask = null;
    }
}
