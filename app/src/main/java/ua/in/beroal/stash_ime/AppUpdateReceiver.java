package ua.in.beroal.stash_ime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("App", "ACTION_MY_PACKAGE_REPLACED");
        /* If this application has been updated,
         * the OS deleted IM subtypes (corresponding to user's keyboards),
         * and a {@link EditKbRepo} must be created. */
        App.getEditKbRepoContext().get(context);
    }
}