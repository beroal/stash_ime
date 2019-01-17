package ua.in.beroal.stash_ime;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import ua.in.beroal.android.ListAdapter;

import static ua.in.beroal.util.Java.splitWords;
import static ua.in.beroal.util.ReactiveX.observableToArrayList;

public class CpvActivity extends AppCompatActivity {
    private ListAdapter<CpvForView> adapter = new ListAdapter<>();

    @NonNull
    public static ArrayList<CpvForView> filteredCpvList(String wordsS) {
        return observableToArrayList(Unicode.filteredCpvObservable(splitWords(wordsS))
                .map(CpvForView::new));
    }

    private void setWords(@NonNull String wordsS) {
        adapter.setData(filteredCpvList(wordsS));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpv);
        final RecyclerView cpvListView = (RecyclerView) findViewById(R.id.search_result);
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
        setWords("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cpv, menu);
        final MenuItem searchMenuItem = menu.findItem(R.id.cpv_menu_search);
        searchMenuItem.expandActionView();
        ((SearchView) searchMenuItem.getActionView())
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String wordsS) {
                        setWords(wordsS);
                        return true;
                    }
                });
        return true;
    }
}