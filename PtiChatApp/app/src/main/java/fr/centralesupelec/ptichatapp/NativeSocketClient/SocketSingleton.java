package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.util.Log;

public class SocketSingleton {

    private static SocketSingleton sInstance;

    private SocketClient mSocketClient;

    private SocketSingleton(Context ctx) {
        mSocketClient = new SocketClient(ctx.getApplicationContext());
    }

    public SocketClient getSocketClient() {
        return mSocketClient;
    }

    /** Access point for the unique instance */
    public static synchronized SocketSingleton getInstance(Context ctx) {
        if (sInstance == null) {
            Log.d("SSc", "✨ Creating the SocketSingleton instance");
            sInstance = new SocketSingleton(ctx);
        }
        return sInstance;
    }
}
