package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.util.Log;

public class SocketSingleton {

    private static SocketSingleton sInstance;

    private SocketClient mSocketClient;
    private boolean mConnected = false;

    private SocketSingleton(Context ctx) {
        mSocketClient = new SocketClient(ctx.getApplicationContext());
    }

    public SocketClient getSocketClient() {
        return mSocketClient;
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public boolean isConnected() {
        return mConnected;
    }

    /** Access point for the unique instance */
    public static synchronized SocketSingleton getInstance(Context ctx) {
        if (sInstance == null) {
            Log.d("SSc", "✨ Creating the SocketSingleton instance");
            sInstance = new SocketSingleton(ctx);
            sInstance.setConnected(true);
        }
        return sInstance;
    }

    public static void renewSocketClient() {
        Log.i("SSr", "♻️ SocketSingleton was asked for renewal");
        if (sInstance != null) {
            (new Thread() {
                public void run() {
                    while (!sInstance.isConnected()) {
                        if (sInstance.mSocketClient.renewSocketClient()) sInstance.setConnected(true);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Log.e("SSs", "♻️ SocketSingleton renewal interrupted?" + e.getMessage());
                        }
                    }
                }
            }).start();
        } else {
            Log.i("SSr", "⭕️ Will not renew empty SocketSingleton instance");
        }
    }
}
