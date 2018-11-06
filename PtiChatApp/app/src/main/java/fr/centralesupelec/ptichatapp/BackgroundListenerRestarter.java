package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BackgroundListenerRestarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BLr", "😨 BackgroundListener stops!");
        context.startService(new Intent(context, BackgroundListener.class));
    }
}
