package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.content.Context;
import android.util.Log;

import java.net.Socket;

public class SocketClientConnection {

    private SocketEmission mSocketEmission;
    private SocketReception mSocketReception;
    private SocketHeartBeat mSocketHeartBeat;

    private Thread mReceptionThread;
    private Thread mHeartBeatThread;

    public void sendMessage(String message) {
        mSocketEmission.sendMessage(message);
    }

    public SocketClientConnection(Socket socket, Context ctx) {
        Log.d("SSc", "✨ Creating the SocketClientConnection instance");

        mSocketEmission = new SocketEmission(socket);
        mSocketReception = new SocketReception(socket, ctx);
        mSocketHeartBeat = new SocketHeartBeat(mSocketEmission);

        mReceptionThread = new Thread(mSocketReception);
        mReceptionThread.start();

        mHeartBeatThread = new Thread(mSocketHeartBeat);
        mHeartBeatThread.start();
    }

    public void close() {
        mSocketEmission.close();
        mSocketReception.close();
        mReceptionThread.interrupt();
        mSocketHeartBeat.close();
        mHeartBeatThread.interrupt();
    }
}
