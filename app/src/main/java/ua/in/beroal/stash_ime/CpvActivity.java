package ua.in.beroal.stash_ime;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;

import ua.in.beroal.android.ListAdapter;

public class CpvActivity extends AppCompatActivity {
    private ListAdapter<CpvForView> adapter = new ListAdapter<>();
    private CpvVm vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = ViewModelProviders.of(this).get(CpvVm.class);
        setContentView(R.layout.activity_cpv);
        final RecyclerView cpvListView = (RecyclerView) findViewById(R.id.cpv_list);
        cpvListView.setHasFixedSize(true);
        cpvListView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setVhFactory(parent -> CpvVh.create(parent,
                cpv -> {
                    final Intent data = new Intent(BuildConfig.APPLICATION_ID + ".Cpv");
                    Cpv.writeToIntent(data, cpv);
                    setResult(RESULT_OK, data);
                    finish();
                }));
        cpvListView.setAdapter(adapter);
        vm.getCpvList().observe(this, adapter::setData);
        ((SearchView) findViewById(R.id.cpv_words))
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String wordsS) {
                        vm.setWordsS(wordsS);
                        return true;
                    }
                });
        vm.restoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveInstanceState(outState);
    }
}