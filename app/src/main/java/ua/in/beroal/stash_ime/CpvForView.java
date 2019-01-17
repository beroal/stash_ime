package ua.in.beroal.stash_ime;

import android.support.annotation.NonNull;

import com.ibm.icu.lang.UProperty;

public class CpvForView {
    private Cpv cpv;
    private String name;

    public CpvForView(Cpv cpv, String name) {
        this.cpv = cpv;
        this.name = name;
    }

    public CpvForView(@NonNull Cpv cpv) {
        this(cpv, cpvToString(cpv));
    }

    @NonNull
    public static String cpvToString(@NonNull Cpv a) {
        return a.getPropertyName(UProperty.NameChoice.LONG)
                + ": " + a.getValueName(UProperty.NameChoice.LONG);
    }


    public Cpv getCpv() {
        return cpv;
    }

    public String getName() {
        return name;
    }
}
