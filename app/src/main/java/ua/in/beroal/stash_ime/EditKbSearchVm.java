package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class EditKbSearchVm extends AndroidViewModel {
    private static final String PORTRAIT_PAGE_FIELD = "portrait_page";
    private boolean initialized = false;
    private MutableLiveData<PortraitPage> portraitPage = new MutableLiveData<>();

    public EditKbSearchVm(@NonNull Application app) {
        super(app);
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PORTRAIT_PAGE_FIELD, portraitPage.getValue().ordinal());
    }

    /**
     * This method must be called before calling other methods except {@link LiveData} getters.
     */
    public void restoreInstanceState(@Nullable Bundle inState) {
        if (!initialized) {
            portraitPage.setValue(inState == null
                    ? PortraitPage.EDIT_KB
                    : PortraitPage.values()[inState.getInt(PORTRAIT_PAGE_FIELD, -1)]);
            initialized = true;
        }
    }

    @NonNull
    public LiveData<PortraitPage> getPortraitPage() {
        return portraitPage;
    }

    public void setPortraitPage(@NonNull PortraitPage portraitPage) {
        this.portraitPage.setValue(portraitPage);
    }

    public enum PortraitPage {EDIT_KB, SEARCH_CHAR}
}
