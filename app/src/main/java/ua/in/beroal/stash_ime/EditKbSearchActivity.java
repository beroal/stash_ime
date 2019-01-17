package ua.in.beroal.stash_ime;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import static ua.in.beroal.util.Android.charToClipboard;

public class EditKbSearchActivity extends AppCompatActivity {
    private ViewGroup portraitV;
    /*private CharClipboardFragment charClipboardFragment;*/
    private EditKbSearchVm vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = ViewModelProviders.of(this).get(EditKbSearchVm.class);
        setContentView(R.layout.activity_edit_kb_search);
        portraitV = (ViewGroup) findViewById(R.id.edit_kb_portrait);
        if (portraitV != null) {
            vm.getPortraitPage().observe(this,
                    portraitPage -> {
                        switch (portraitPage) {
                            case EDIT_KB:
                                getSupportFragmentManager().beginTransaction()
                                        .show(getSupportFragmentManager().findFragmentById(R.id.edit_kb_fragment))
                                        .hide(getSupportFragmentManager().findFragmentById(R.id.search_char_fragment))
                                        .commit();
                                break;
                            case SEARCH_CHAR:
                                getSupportFragmentManager().beginTransaction()
                                        .hide(getSupportFragmentManager().findFragmentById(R.id.edit_kb_fragment))
                                        .show(getSupportFragmentManager().findFragmentById(R.id.search_char_fragment))
                                        .commit();
                                break;
                        }
                    });
        }
        /*charClipboardFragment = (CharClipboardFragment) getSupportFragmentManager()
                .findFragmentById(R.id.char_clipboard_fragment);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (portraitV == null) {
            return false;
        } else {
            getMenuInflater().inflate(R.menu.edit_kb_portrait_switch, menu);
            return true;
        }

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

    /*@Override
    protected void onDestroy() {
        charClipboardFragment = null;
        super.onDestroy();
    }*/

    /*@Override
    public void copy(int char1) {
        charToClipboard(getApplicationContext(), char1);
        charClipboardFragment.onInsertFirstChar(char1);
    }*/
}
