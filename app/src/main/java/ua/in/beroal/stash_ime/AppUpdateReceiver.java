package ua.in.beroal.stash_ime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction() == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("App", "ACTION_MY_PACKAGE_REPLACED");
            /* If this application has been updated,
             * the OS deleted this input method's subtypes (corresponding to user's keyboards),
             * so the subtypes need to be sent to OS by creating a {@link EditKbRepo}. */
            App.getEditKbRepoContext().get(context);
        }
    }
}