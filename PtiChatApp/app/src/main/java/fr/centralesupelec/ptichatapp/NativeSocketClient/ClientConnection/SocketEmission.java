package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketEmission {

    private PrintWriter mOut;

    SocketEmission(Socket socket) {
        try {
            mOut = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            Log.e("CCq", "😿 Could not create SocketEmission: " + e);
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        mOut.println(message);
        mOut.flush();
    }
}
