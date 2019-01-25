package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
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
import ua.in.beroal.android.ClickPointGridLayout;
import ua.in.beroal.android.ClickPointTextView;
import ua.in.beroal.java.NoMatchingConstant;
import ua.in.beroal.util.Unicode;

public class EditKbFragment extends Fragment {
    public static final String JSON_MIME_TYPE = "application/json";
    public static final int EXPORT_REQUEST_CODE = 0;
    public static final int IMPORT_REQUEST_CODE = 1;
    private EditKbVm vm;
    private String insertKbName;

    private static int getHitDelete(Iterable<Integer> bounds, int pointer) {
        int i = 0;
        for (int bound : bounds) {
            if (pointer < bound) {
                return i;
            }
            i++;
        }
        return i;
    }

    private static int getHitInsert(Iterable<Integer> bounds, int pointer) {
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

    private static int pointerToLineIx(
            float pointer, Iterable<Integer> bounds, EditKbModeLine.Op op) {
        final int pointerInteger = Math.round(pointer);
        switch (op) {
            case DELETE:
                return getHitDelete(bounds, pointerInteger);
            case INSERT:
                return getHitInsert(bounds, pointerInteger);
            default:
                throw new NoMatchingConstant();
        }
    }

    @NonNull
    private static String getOpButtonText(@NonNull EditKbModeLine editMode) {
        switch (editMode.getOp()) {
            case DELETE:
                return "−";
            case INSERT:
                return "+";
            default:
                throw new NoMatchingConstant();
        }
    }

    @NonNull
    private static String getCoordButtonText(@NonNull EditKbModeLine editMode) {
        switch (editMode.getCoord()) {
            case ROW:
                return "R";
            case COLUMN:
                return "C";
            default:
                throw new NoMatchingConstant();
        }
    }

    /**
     * Initializes a button with {@code buttonId} resource id
     * and {@code editMode} function (purpose).
     * {@link #vm} must be initialized.
     */
    private void initEditModeButton(@NonNull View rootView,
                                    int buttonId, @NonNull EditKbModeLine editMode) {
        final Button buttonView = (Button) rootView.findViewById(buttonId);
        buttonView.setText(getOpButtonText(editMode) + getCoordButtonText(editMode));
        vm.getEditMode(editMode).observe(this,
                isOn -> {
                    buttonView.setTextColor(isOn ? 0xFFFFFFFF : 0xFF555555);
                    buttonView.setSelected(isOn);
                });
        buttonView.setOnClickListener(view -> vm.flipEditModeLine(editMode));
    }

    /**
     * Sets the adapter of {@code chooseKbView}. {@link #vm} must be initialized.
     */
    private void setChooseKbAdapter(@NonNull Spinner chooseKbView) {
        ArrayAdapter<CharSequence> chooseKbAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        chooseKbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseKbView.setAdapter(chooseKbAdapter);
        vm.getKbListSel().observe(this,
                a -> {
                    chooseKbAdapter.clear();
                    for (CharSequence item : a.second) {
                        chooseKbAdapter.add(item);
                    }
                    chooseKbAdapter.notifyDataSetChanged();
                    chooseKbView.setSelection(a.first != -1
                            ? a.first : AdapterView.INVALID_POSITION);

                });
    }

    private void initKbMenu(@NonNull View kbShowMenuView) {
        final PopupMenu popupMenu = new PopupMenu(getContext(), kbShowMenuView);
        popupMenu.inflate(R.menu.kb);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.insert_kb_form_flip:
                    vm.flipInsertKbForm();
                    break;
                case R.id.enable_kb:
                    App.getInputMethodManager()
                            .get(getContext().getApplicationContext())
                            .showInputMethodAndSubtypeEnabler(
                                    App.getThisInputMethodId()
                                            .get(getContext().getApplicationContext()));
                    break;
                case R.id.export_kb:
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(JSON_MIME_TYPE);
                    intent.putExtra(Intent.EXTRA_TITLE, vm.getExportKbFileName());
                    startActivityForResult(intent, EXPORT_REQUEST_CODE);
                    break;
                case R.id.delete_kb:
                    vm.deleteKb();
                    break;
                default:
                    throw new NoMatchingConstant("unknown kb menu item");
            }
            return true;
        });
        kbShowMenuView.setOnClickListener(view -> popupMenu.show());
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        vm = ViewModelProviders.of(this).get(EditKbVm.class);
        final View rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_edit_kb, parent, false);
        initKbMenu(rootView.findViewById(R.id.kb_menu));
        vm.getIsInsertKbFormShown().observe(this,
                a -> rootView.findViewById(R.id.insert_kb_form).setVisibility(
                        a ? View.VISIBLE : View.GONE));
        final ViewGroup editKbView = rootView.findViewById(R.id.edit_kb);
        vm.getKb().observe(this, kbViewState -> createKbView(editKbView, kbViewState));
        {
            Spinner chooseKbView = (Spinner) rootView.findViewById(R.id.choose_kb);
            chooseKbView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    vm.chooseKb(position == AdapterView.INVALID_POSITION ? -1 : position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            setChooseKbAdapter(chooseKbView);
        }
        final TextView insertKbNameView = (TextView) rootView.findViewById(R.id.insert_kb_name);
        ((Button) rootView.findViewById(R.id.insert_kb_create)).setOnClickListener(
                view -> {
                    vm.hideInsertKbForm();
                    vm.insertKbCreate(insertKbNameView.getText().toString());
                }
        );
        ((Button) rootView.findViewById(R.id.insert_kb_import)).setOnClickListener(
                view -> {
                    insertKbName = insertKbNameView.getText().toString();
                    vm.hideInsertKbForm();
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(JSON_MIME_TYPE);
                    startActivityForResult(intent, IMPORT_REQUEST_CODE);
                }
        );
        rootView.findViewById(R.id.insert_kb_cancel)
                .setOnClickListener(view -> vm.hideInsertKbForm());
        initEditModeButton(rootView, R.id.insert_row, new EditKbModeLine(
                EditKbModeLine.Op.INSERT, EditKbModeLine.Coord.ROW));
        initEditModeButton(rootView, R.id.insert_column, new EditKbModeLine(
                EditKbModeLine.Op.INSERT, EditKbModeLine.Coord.COLUMN));
        initEditModeButton(rootView, R.id.delete_row, new EditKbModeLine(
                EditKbModeLine.Op.DELETE, EditKbModeLine.Coord.ROW));
        initEditModeButton(rootView, R.id.delete_column, new EditKbModeLine(
                EditKbModeLine.Op.DELETE, EditKbModeLine.Coord.COLUMN));
        vm.restoreInstanceState(savedInstanceState);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        ((Activity) getContext()).getMenuInflater().inflate(R.menu.edit_kb_key_context, menu);
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

    private boolean keyViewOnDrag(Pair<Integer, Integer> pos, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            case DragEvent.ACTION_DROP:
                final ClipData clipData = event.getClipData();
                if (clipData.getItemCount() != 0) {
                    final CharSequence a = clipData.getItemAt(0).getText();
                    vm.putKeyIfFilled(pos,
                            a == null || a.length() == 0
                                    ? Unicode.NO_CHAR : UCharacter.codePointAt(a, 0));
                }
                return true;
            default:
                return true;
        }
    }

    @NonNull
    private ClickPointTextView createKeyView(
            boolean isEditKbModeKey, int rowI, int columnI, int char1) {
        final ClickPointTextView keyView = new ClickPointTextView(getContext());
        final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                GridLayout.spec(rowI, GridLayout.FILL),
                GridLayout.spec(columnI, GridLayout.FILL, 1F));
        keyView.setLayoutParams(layoutParams);
        keyView.setGravity(Gravity.CENTER);
        keyView.setTextSize(22);
        keyView.setBackgroundColor(0xFFFFEECC);
        keyView.setText(char1 == Unicode.NO_CHAR
                ? "" : Unicode.codePointToString(char1));
        final Pair<Integer, Integer> pos = new Pair<>(rowI, columnI);
        keyView.setTag(R.id.key_pos, pos);
        final boolean keyIsEmpty = char1 == Unicode.NO_CHAR;
        if (isEditKbModeKey) {
            keyView.setOnClickPointListener((view, x, y) -> {
                if (keyIsEmpty) {
                    vm.pasteKey(pos);
                } else {
                    registerForContextMenu(keyView);
                    view.showContextMenu(x, y);
                }
            });
            keyView.setOnDragStartedListener((view, x, y) -> {
                view.startDragAndDrop(
                        ClipData.newPlainText("", Unicode.codePointToString(char1)),
                        new View.DragShadowBuilder(view),
                        null,
                        0);
                vm.clearKey(pos);
            });
            keyView.setOnDragListener((view, event) -> keyViewOnDrag(pos, event));
        } else {
            keyView.setClickable(false);
        }
        return keyView;
    }

    private void createKbView(ViewGroup editKbView, KbViewState kbViewState) {
        if (editKbView.getChildCount() != 0) {
            editKbView.removeViewAt(0);
        }
        if (!kbViewState.getKeys().isEmpty()) {
            final ClickPointGridLayout gridView = new ClickPointGridLayout(getContext());
            final Pair<List<View>, List<View>> gridMeasure =
                    new Pair<>(new ArrayList<>(), new ArrayList<>());
            gridView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            gridView.setUseDefaultMargins(true);
            final KbKeys kb = kbViewState.getKeys().orElseThrow();
            gridView.setColumnCount(kb.getColumnCount());
            {
                int rowI = 0;
                for (Iterable<Integer> keyRow : kb.getKeys()) {
                    int columnI = 0;
                    for (int char1 : keyRow) {
                        final ClickPointTextView keyView = createKeyView(
                                kbViewState.getEditMode() instanceof EditKbModeKey,
                                rowI, columnI, char1);
                        gridView.addView(keyView);
                        if (rowI == 0) {
                            gridMeasure.second.add(keyView);
                        }
                        if (columnI == 0) {
                            gridMeasure.first.add(keyView);
                        }
                        columnI++;
                    }
                    rowI++;
                }
            }
            if (kbViewState.getEditMode() instanceof EditKbModeLine) {
                EditKbModeLine editModeLine = (EditKbModeLine) kbViewState.getEditMode();
                gridView.setOnClickPointListener((view, x, y) -> {
                    final Iterable<View> boundViews;
                    final Function<View, Integer> viewToBound;
                    final float pointer;
                    switch (editModeLine.getCoord()) {
                        case ROW:
                            boundViews = gridMeasure.first;
                            viewToBound = View::getBottom;
                            pointer = y;
                            break;
                        case COLUMN:
                            boundViews = gridMeasure.second;
                            viewToBound = View::getRight;
                            pointer = x;
                            break;
                        default:
                            throw new NoMatchingConstant();
                    }
                    final Iterable<Integer> bounds = Observable.fromIterable(boundViews)
                            .map(viewToBound)
                            .blockingIterable();
                    vm.editModeDoOp(pointerToLineIx(pointer, bounds, editModeLine.getOp()));
                });
            }
            editKbView.addView(gridView);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                final Uri uri = data.getData();
                Log.d("App", "EditKbFragment.onActivityResult.EXPORT_REQUEST_CODE=" + uri);
                vm.exportKb(uri);
            }
        } else if (requestCode == IMPORT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                final Uri uri = data.getData();
                Log.d("App", "EditKbFragment.onActivityResult.IMPORT_REQUEST_CODE=" + uri);
                vm.insertKbImport(insertKbName, uri);
                insertKbName = null;
            }
        } else {
            throw new IllegalArgumentException("unknown request code");
        }
    }
}
