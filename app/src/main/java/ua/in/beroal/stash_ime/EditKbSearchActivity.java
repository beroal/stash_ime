package ua.in.beroal.stash_ime;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * We use {@link FrameLayout} instead of {@link android.support.v4.view.ViewPager}
 * because we need every fragment to have the same identifier (id)
 * in all layout XML files for this activity,
 * and identifiers of fragments inside a {@link android.support.v4.view.ViewPager} can't be set.
 * We need every fragment to have the same identifier
 * so fragment state is preserved when changing screen orientation.
 *
 * @see <a href="https://coderanch.com/t/602443/Dynamic-Fragments-access-recreated-saved">
 * Dynamic Fragments: How can I access them when they are recreated from saved state?</a>
 */
public class EditKbSearchActivity extends AppCompatActivity {
    public static final String PORTRAIT_PAGE_FIELD = "portrait_page";
    private int portraitPage;

    private static void setPortraitPageVisibility(@NonNull View[] fragmentShow, int portraitPage) {
        for (int i = fragmentShow.length; i-- != 0; ) {
            fragmentShow[i].setVisibility(portraitPage == i ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_kb_search);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.eks_tab_layout);
        final boolean portrait = tabLayout != null;
        portraitPage = savedInstanceState != null
                ? savedInstanceState.getInt(PORTRAIT_PAGE_FIELD)
                : 0;
        if (portrait) {
            final View[] fragmentShow = {
                    findViewById(R.id.edit_kb_fragment_show),
                    findViewById(R.id.search_char_fragment_show)};
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    portraitPage = tab.getPosition();
                    setPortraitPageVisibility(fragmentShow, portraitPage);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
            setPortraitPageVisibility(fragmentShow, portraitPage);
            tabLayout.getTabAt(portraitPage).select();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PORTRAIT_PAGE_FIELD, portraitPage);
    }
}
