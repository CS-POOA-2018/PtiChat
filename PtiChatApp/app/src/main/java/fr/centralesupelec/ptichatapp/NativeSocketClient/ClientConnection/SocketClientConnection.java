package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.content.Context;
import android.util.Log;

import java.net.Socket;

public class SocketClientConnection {

    private SocketEmission mSocketEmission;

    public void sendMessage(String message) {
        mSocketEmission.sendMessage(message);
    }

    public SocketClientConnection(Socket socket, Context ctx) {
        Log.d("SSc", "✨ Creating the SocketClientConnection instance");

        mSocketEmission = new SocketEmission(socket);
        SocketReception socketReception = new SocketReception(socket, ctx);

        Thread receptionThread = new Thread(socketReception);
        receptionThread.start();
    }
}
