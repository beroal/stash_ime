package ua.in.beroal.stash_ime;

import android.content.Intent;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ua.in.beroal.android.ListAdapter;

public class CpvActivity extends AppCompatActivity {
    private ListAdapter<Pair<String, Cpv>> adapter = new ListAdapter<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpv);
        final RecyclerView searchCharResultV = (RecyclerView) findViewById(R.id.search_result);
        searchCharResultV.setHasFixedSize(true);
        searchCharResultV.setLayoutManager(new LinearLayoutManager(this));
        {
            adapter.setVhFactory(parent -> CpvVh.create(parent,
                    cpv -> {
                        final Intent data = new Intent(
                                "ua.in.beroal.android.main_academy.lab.CharPropertyValue");
                        Cpv.writeToIntent(data, cpv);
                        setResult(RESULT_OK, data);
                        finish();
                    }));
            setWords("");
            searchCharResultV.setAdapter(adapter);
        }
    }

    private void setWords(String wordsS) {
        adapter.setData(Unicode.filteredCpvList(wordsS));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_char_property, menu);
        final MenuItem menuItem = menu.findItem(R.id.char_property_search);
        menuItem.expandActionView();
        final SearchView v = (SearchView) menuItem.getActionView();
        v.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String wordsS) {
                Log.i("Search", "onQueryTextChange: " + wordsS);
                setWords(wordsS);
                return true;
            }
        });
        return true;
    }
}