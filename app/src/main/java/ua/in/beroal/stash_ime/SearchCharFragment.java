package ua.in.beroal.stash_ime;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private ViewGroup cpvFilterV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = ViewModelProviders.of(this).get(SearchCharVm.class);
        setHasOptionsMenu(true);
    }

    private void setCpvFilterV(Iterable<String> charPropertyValues) {
        if (cpvFilterV.getChildCount() != 0) {
            cpvFilterV.removeViewAt(0);
        }
        final LinearLayout listV = new LinearLayout(getContext());
        listV.setOrientation(LinearLayout.VERTICAL);
        int i = 0;
        for (String name : charPropertyValues) {
            final View item = getLayoutInflater().inflate(
                    R.layout.cpv_filter_item, listV, false);
            ((TextView) item.findViewById(R.id.char_image)).setText(name);
            final View deleteV = item.findViewById(R.id.delete);
            deleteV.setTag(i);
            deleteV.setOnClickListener(v -> vm.deleteFilterItem((Integer) v.getTag()));
            listV.addView(item);
            i++;
        }
        cpvFilterV.addView(listV, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootV = inflater.inflate(R.layout.fragment_search_char, container, false);
        final EditText needlesV = (EditText) rootV.findViewById(R.id.words_s);
        needlesV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("Search", "needles.onKey: " + needlesV.getText().toString());
                vm.setWordsS(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cpvFilterV = (ViewGroup) rootV.findViewById(R.id.cpv_filter);
        vm.getCpvFilterLd().observe(this, this::setCpvFilterV);
        {
            final RecyclerView searchCharResultV = (RecyclerView) rootV.findViewById(R.id.search_char_result);
            searchCharResultV.setHasFixedSize(true);
            searchCharResultV.setLayoutManager(new LinearLayoutManager(getContext()));
            final ListAdapter<CharRow> charRowListAdapter = new ListAdapter<>();
            charRowListAdapter.setVhFactory(parent -> SearchCharVh.create(parent, vm::copyChar));
            searchCharResultV.setAdapter(charRowListAdapter);
            vm.getCharListLd().observe(this, charRowListAdapter::setData);
        }
        ((Button) rootV.findViewById(R.id.insert_cpv_filter))
                .setOnClickListener(v -> startActivityForResult(
                        new Intent(getContext(), CpvActivity.class), 0));
        vm.restoreInstanceState(savedInstanceState);
        return rootV;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            final Cpv cpv = Cpv.readFromIntent(data);
            if (cpv != null) {
                vm.insertFilterItem(cpv);
            }
        }
    }

}
