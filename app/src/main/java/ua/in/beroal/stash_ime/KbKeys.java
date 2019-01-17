package ua.in.beroal.stash_ime;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static ua.in.beroal.util.ReactiveX.observableToArrayList;

public class KbKeys implements KbKeysOptional {
    private final int version = 0;
    private List<List<Integer>> keys;

    public KbKeys() {
        final List<Integer> row = new ArrayList<>(1);
        row.add(-1);
        keys = new ArrayList<>(1);
        keys.add(row);
    }

    public KbKeys(List<List<Integer>> keys) {
        this.keys = keys;
    }

    public KbKeys clone() {
        return new KbKeys(observableToArrayList(
                Observable.fromIterable(keys)
                        .map(ArrayList::new)));
    }

    public List<List<Integer>> getKeys() {
        return keys;
    }

    public int getColumnCount() {
        return keys.get(0).size();
    }

    public int getKey(Pair<Integer, Integer> pos) {
        return keys.get(pos.first).get(pos.second);
    }

    public void putKey(Pair<Integer, Integer> pos, int char1) {
        keys.get(pos.first).set(pos.second, char1);
    }

    void editLineDoOp(EditKbModeLine editMode, int i) {
        switch (editMode.getCoord()) {
            case 0:
                switch (editMode.getOp()) {
                    case 0:
                        keys.remove(i);
                        break;
                    case 1:
                        keys.add(i, new ArrayList<>(Collections.nCopies(getColumnCount(), -1)));
                        break;
                }
                break;
            case 1:
                switch (editMode.getOp()) {
                    case 0:
                        for (List<Integer> row : keys) {
                            row.remove(i);
                        }
                        break;
                    case 1:
                        for (List<Integer> row : keys) {
                            row.add(i, -1);
                        }
                        break;
                }
                break;
        }
    }
}
