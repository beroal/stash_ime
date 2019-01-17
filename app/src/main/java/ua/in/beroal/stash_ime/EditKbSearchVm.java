package ua.in.beroal.stash_ime;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

public class EditKbSearchVm extends AndroidViewModel {
    public enum PortraitPage { EDIT_KB, SEARCH_CHAR }
    private MutableLiveData<PortraitPage> portraitPage = new MutableLiveData<>();

    public EditKbSearchVm(@NonNull Application app) {
        super(app);
        portraitPage.setValue(PortraitPage.EDIT_KB);
    }

    public LiveData<PortraitPage> getPortraitPage() {
        return portraitPage;
    }

    public void setPortraitPage(PortraitPage portraitPage) {
        this.portraitPage.setValue(portraitPage);
    }
}
