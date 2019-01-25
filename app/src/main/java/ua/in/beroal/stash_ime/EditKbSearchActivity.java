package ua.in.beroal.stash_ime;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import ua.in.beroal.java.NoMatchingConstant;

public class EditKbSearchActivity extends AppCompatActivity {
    private EditKbSearchVm vm;
    private ViewGroup portraitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = ViewModelProviders.of(this).get(EditKbSearchVm.class);
        setContentView(R.layout.activity_edit_kb_search);
        portraitView = (ViewGroup) findViewById(R.id.edit_kb_portrait);
        final Fragment editKbFragment = getSupportFragmentManager()
                .findFragmentById(R.id.edit_kb_fragment);
        final Fragment searchCharFragment = getSupportFragmentManager()
                .findFragmentById(R.id.search_char_fragment);
        if (portraitView != null) {
            vm.getPortraitPage().observe(this,
                    portraitPage -> {
                        switch (portraitPage) {
                            case EDIT_KB:
                                getSupportFragmentManager().beginTransaction()
                                        .show(editKbFragment)
                                        .hide(searchCharFragment)
                                        .commit();
                                break;
                            case SEARCH_CHAR:
                                getSupportFragmentManager().beginTransaction()
                                        .hide(editKbFragment)
                                        .show(searchCharFragment)
                                        .commit();
                                break;
                            default:
                                throw new NoMatchingConstant();
                        }
                    });
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(editKbFragment)
                    .show(searchCharFragment)
                    .commit();
        }
        vm.restoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (portraitView != null) {
            getMenuInflater().inflate(R.menu.edit_kb_portrait_switch, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.portrait_switch_edit_kb:
                vm.setPortraitPage(EditKbSearchVm.PortraitPage.EDIT_KB);
                return true;
            case R.id.portrait_switch_search_char:
                vm.setPortraitPage(EditKbSearchVm.PortraitPage.SEARCH_CHAR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
