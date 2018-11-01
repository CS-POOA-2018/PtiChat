package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.util.Log;

public class SocketHeartBeat implements Runnable {

    private boolean mClosed;
    private SocketEmission mSocketEmission;

    SocketHeartBeat(SocketEmission socketEmission) {
        mSocketEmission = socketEmission;
        mClosed = false;
    }

    @Override
    public void run() {
        while (!mClosed) {  // Always active until connection is closed or broken
            try {
                Thread.sleep(30000);
                mSocketEmission.sendMessage("heartBeat");
            } catch (InterruptedException e) {
                Log.w("HBi", "HeartBeat interrupted </3");
            }
        }
    }

    void close() {
        mClosed = true;
    }
}
