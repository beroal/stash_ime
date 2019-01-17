package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibm.icu.lang.UCharacter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import ua.in.beroal.android.ClickPointTextView;
import ua.in.beroal.util.Unicode;

public class EditKbFragment extends Fragment {
    private EditKbVm vm;
    private View rootV;
    private ViewGroup editKbV;
    private Spinner chooseKbV;
    private ArrayAdapter<CharSequence> chooseKbAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        vm = ViewModelProviders.of(this).get(EditKbVm.class);
        rootV = (ViewGroup) inflater.inflate(R.layout.fragment_edit_kb, parent, false);
        ((Button) rootV.findViewById(R.id.insert_kb_form_flip))
                .setOnClickListener(v -> vm.flipInsertKbForm());
        vm.getIsInsertKbFormShown().observe(this,
                a -> rootV.findViewById(R.id.insert_kb_form).setVisibility(
                        a ? View.VISIBLE : View.GONE));
        editKbV = rootV.findViewById(R.id.edit_kb);
        vm.getKbLiveData().observe(this,
                kbStateV -> {
                    if (editKbV.getChildCount() != 0) {
                        editKbV.removeViewAt(0);
                    }
                    if (!kbStateV.getKeys().isEmpty()) {
                        final KbKeys kb = kbStateV.getKeys().orElseThrow();
                        final KbGridView gridV = new KbGridView(getContext());
                        final Pair<List<View>, List<View>> gridMeasure =
                                new Pair<>(new ArrayList<>(), new ArrayList<>());
                        gridV.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        gridV.setUseDefaultMargins(true);
                        gridV.setColumnCount(kb.getColumnCount());
                        int i = 0;
                        for (Iterable<Integer> keyRow : kb.getKeys()) {
                            int j = 0;
                            for (int char1 : keyRow) {
                                final ClickPointTextView keyV = new ClickPointTextView(getContext());
                                if (i == 0) {
                                    gridMeasure.second.add(keyV);
                                }
                                if (j == 0) {
                                    gridMeasure.first.add(keyV);
                                }
                                final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                                        GridLayout.spec(i, GridLayout.FILL),
                                        GridLayout.spec(j, GridLayout.FILL, 1F));
                                keyV.setLayoutParams(layoutParams);
                                keyV.setGravity(Gravity.CENTER);
                                keyV.setTextSize(22);
                                keyV.setBackgroundColor(0xFFFFEECC);
                                keyV.setText(char1 == -1 ? "" : Unicode.codePointToString(char1));
                                final Pair<Integer, Integer> pos = new Pair<>(i, j);
                                keyV.setTag(R.id.key_pos, pos);
                                final boolean keyIsEmpty = char1 == -1;
                                if (kbStateV.getEditMode() instanceof EditKbModeKey) {
                                    keyV.setOnClickPointListener((v, x, y) -> {
                                        if (keyIsEmpty) {
                                            vm.pasteKey(pos);
                                        } else {
                                            registerForContextMenu(keyV);
                                            v.showContextMenu(x, y);
                                        }
                                    });
                                    keyV.setOnDragFromListener(v -> {
                                        v.startDragAndDrop(
                                                ClipData.newPlainText("", Unicode.codePointToString(char1)),
                                                new View.DragShadowBuilder(v),
                                                null,
                                                0);
                                        vm.putKey(pos, -1);
                                    });
                                    keyV.setOnDragListener((v, event) -> {
                                        switch (event.getAction()) {
                                            case DragEvent.ACTION_DRAG_STARTED:
                                                return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                                            case DragEvent.ACTION_DROP:
                                                final ClipData clipData = event.getClipData();
                                                if (clipData.getItemCount() != 0) {
                                                    final CharSequence b = clipData.getItemAt(0).getText();
                                                    int char2 = b == null || b.length() == 0 ? -1 : UCharacter.codePointAt(b, 0);
                                                    vm.putKey(pos, char2);
                                                }
                                                return true;
                                            default:
                                                return true;
                                        }
                                    });
                                } else {
                                    keyV.setClickable(false);
                                }
                                gridV.addView(keyV);
                                j++;
                            }
                            i++;
                        }
                        if (kbStateV.getEditMode() instanceof EditKbModeLine) {
                            EditKbModeLine editModeLine = (EditKbModeLine) kbStateV.getEditMode();
                            gridV.setOnClickPointListener((v, x, y) -> {
                                final Iterable<View> boundViews;
                                final Function<View, Integer> viewToBound;
                                final float pointer;
                                switch (editModeLine.getCoord()) {
                                    case 0:
                                        boundViews = gridMeasure.first;
                                        viewToBound = View::getBottom;
                                        pointer = y;
                                        break;
                                    case 1:
                                        boundViews = gridMeasure.second;
                                        viewToBound = View::getRight;
                                        pointer = x;
                                        break;
                                    default:
                                        throw new RuntimeException();
                                }
                                final Iterable<Integer> bounds = Observable.fromIterable(boundViews)
                                        .map(viewToBound)
                                        .blockingIterable();
                                final int pointerI = Math.round(pointer);
                                final int i1;
                                switch (editModeLine.getOp()) {
                                    case 0:
                                        i1 = getHitDelete(bounds, pointerI);
                                        break;
                                    case 1:
                                        i1 = getHitInsert(bounds, pointerI);
                                        break;
                                    default:
                                        throw new RuntimeException();
                                }
                                vm.editModeDoOp(i1);
                            });
                        }
                        editKbV.addView(gridV);
                    }
                });
        chooseKbV = (Spinner) rootV.findViewById(R.id.choose_kb);
        chooseKbV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vm.setChosenKb(position == AdapterView.INVALID_POSITION ? -1 : position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        chooseKbAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item);
        chooseKbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseKbV.setAdapter(chooseKbAdapter);
        vm.getKbListLiveData().observe(this,
                a -> {
                    chooseKbAdapter.clear();
                    for (CharSequence item : a.second) {
                        chooseKbAdapter.add(item);
                    }
                    chooseKbAdapter.notifyDataSetChanged();
                    if (a.first != -1) {
                        chooseKbV.setSelection(a.first);
                    } else {
                        chooseKbV.setSelection(AdapterView.INVALID_POSITION);
                    }

                });
        ((Button) rootV.findViewById(R.id.insert_kb_do)).setOnClickListener(
                v -> {
                    vm.hideInsertKbForm();
                    vm.insertKb(((TextView) rootV.findViewById(R.id.insert_kb_name)).getText().toString());
                }
        );
        final Button deleteKbV = (Button) rootV.findViewById(R.id.delete_kb);
        deleteKbV.setOnClickListener(v -> vm.deleteKb());
        initEditModeButton(R.id.insert_row, new EditKbModeLine(1, 0));
        initEditModeButton(R.id.insert_column, new EditKbModeLine(1, 1));
        initEditModeButton(R.id.delete_row, new EditKbModeLine(0, 0));
        initEditModeButton(R.id.delete_column, new EditKbModeLine(0, 1));
        vm.restoreInstanceState(savedInstanceState);
        return rootV;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.onSaveInstanceState(outState);
    }

    private int getHitDelete(Iterable<Integer> bounds, int pointer) {
        int i = 0;
        for (int bound : bounds) {
            if (pointer < bound) {
                return i;
            }
            i++;
        }
        return i;
    }

    private int getHitInsert(Iterable<Integer> bounds, int pointer) {
        int i = 0;
        int prevBound = 0;
        for (int bound : bounds) {
            if (pointer < (prevBound + bound) / 2) {
                return i;
            }
            prevBound = bound;
            i++;
        }
        return i;
    }

    private void initEditModeButton(int buttonId, EditKbModeLine editMode) {
        final Button buttonV = (Button) rootV.findViewById(buttonId);
        final String opS;
        switch (editMode.getOp()) {
            case 0:
                opS = "−";
                break;
            case 1:
                opS = "+";
                break;
            default:
                throw new IllegalArgumentException();
        }
        final String coordS;
        switch (editMode.getCoord()) {
            case 0:
                coordS = "R";
                break;
            case 1:
                coordS = "C";
                break;
                default:
                    throw new IllegalArgumentException();
        }
        buttonV.setText(opS + coordS);
        vm.getEditModeLiveData(editMode).observe(this,
                isOn -> {
                    buttonV.setTextColor(isOn ? 0xFFFFFFFF : 0xFF555555);
                    buttonV.setBackgroundColor(isOn ? 0xFF555555 : 0xFFDDDDDD);
                });
        buttonV.setOnClickListener(v -> vm.flipEditModeLine(editMode));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        ((Activity) getContext()).getMenuInflater().inflate(R.menu.kb_key_context, menu);
        final Pair<Integer, Integer> pos = (Pair<Integer, Integer>) v.getTag(R.id.key_pos);
        menu.findItem(R.id.kb_key_clear).setOnMenuItemClickListener(
                item -> {
                    vm.clearKey(pos);
                    return true;
                });
        menu.findItem(R.id.kb_key_copy).setOnMenuItemClickListener(
                item -> {
                    vm.copyKey(pos);
                    return true;
                });
    }
}
