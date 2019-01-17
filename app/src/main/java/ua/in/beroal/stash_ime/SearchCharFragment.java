package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ua.in.beroal.android.ListAdapter;

public class SearchCharFragment extends Fragment {
    private SearchCharVm vm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = ViewModelProviders.of(this).get(SearchCharVm.class);
    }

    /**
     * Updates the CPV filter.
     */
    private void setCpvFilterView(ViewGroup cpvFilterView, @NonNull Iterable<String> cpvList) {
        if (cpvFilterView.getChildCount() != 0) {
            cpvFilterView.removeViewAt(0);
        }
        final LinearLayout listView = new LinearLayout(getContext());
        listView.setOrientation(LinearLayout.VERTICAL);
        int i = 0;
        for (String name : cpvList) {
            final View item = getLayoutInflater().inflate(
                    R.layout.cpv_filter_item, listView, false);
            ((TextView) item.findViewById(R.id.cpv_filter_item_name)).setText(name);
            {
                final View deleteView = item.findViewById(R.id.cpv_filter_item_delete);
                deleteView.setTag(i);
                deleteView.setOnClickListener(v -> vm.deleteFilterItem((Integer) v.getTag()));
            }
            listView.addView(item);
            i++;
        }
        cpvFilterView.addView(listView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Initializes the list of characters. {@link #vm} must be initialized.
     */
    private void initCharRowList(@NonNull View rootView) {
        final RecyclerView searchCharResultView =
                (RecyclerView) rootView.findViewById(R.id.search_char_result);
        searchCharResultView.setHasFixedSize(true);
        searchCharResultView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ListAdapter<CharForView> charRowListAdapter = new ListAdapter<>();
        charRowListAdapter.setVhFactory(parent -> SearchCharVh.create(parent, vm::copyChar));
        searchCharResultView.setAdapter(charRowListAdapter);
        vm.getCharList().observe(this, charRowListAdapter::setData);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_char, container, false);
        final EditText wordsView = (EditText) rootView.findViewById(R.id.words_s);
        wordsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setWordsS(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final ViewGroup cpvFilterView = rootView.findViewById(R.id.cpv_filter);
        vm.getCpvFilter().observe(this, cpvList -> setCpvFilterView(cpvFilterView, cpvList));
        initCharRowList(rootView);
        ((Button) rootView.findViewById(R.id.insert_cpv_filter))
                .setOnClickListener(view -> startActivityForResult(
                        new Intent(getContext(), CpvActivity.class), 0));
        vm.restoreInstanceState(savedInstanceState);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Cpv cpv = Cpv.readFromIntent(data);
            if (cpv != null) {
                vm.insertFilterItem(cpv);
            }
        }
    }
}
