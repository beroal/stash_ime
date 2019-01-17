package ua.in.beroal.stash_ime;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import ua.in.beroal.java.NoMatchingConstant;
import ua.in.beroal.util.Unicode;

import static ua.in.beroal.util.ReactiveX.observableToArrayList;

public class KbKeys {
    private int version = 0;
    private List<List<Integer>> keys;

    public KbKeys() {
        final List<Integer> row = new ArrayList<>(1);
        row.add(Unicode.NO_CHAR);
        keys = new ArrayList<>(1);
        keys.add(row);
    }

    public KbKeys(@NonNull List<List<Integer>> keys) {
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

    public int getKey(@NonNull Pair<Integer, Integer> pos) {
        return keys.get(pos.first).get(pos.second);
    }

    public void putKey(@NonNull Pair<Integer, Integer> pos, int char1) {
        keys.get(pos.first).set(pos.second, char1);
    }

    public void editLineDoOp(@NonNull EditKbModeLine editMode, int i) {
        switch (editMode.getCoord()) {
            case ROW:
                switch (editMode.getOp()) {
                    case DELETE:
                        keys.remove(i);
                        break;
                    case INSERT:
                        final ArrayList<Integer> row = new ArrayList<>(
                                Collections.nCopies(getColumnCount(), Unicode.NO_CHAR));
                        keys.add(i, row);
                        break;
                    default:
                        throw new NoMatchingConstant();
                }
                break;
            case COLUMN:
                switch (editMode.getOp()) {
                    case DELETE:
                        for (List<Integer> row : keys) {
                            row.remove(i);
                        }
                        break;
                    case INSERT:
                        for (List<Integer> row : keys) {
                            row.add(i, Unicode.NO_CHAR);
                        }
                        break;
                    default:
                        throw new NoMatchingConstant();
                }
                break;
            default:
                throw new NoMatchingConstant();
        }
    }
}
