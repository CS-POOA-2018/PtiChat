package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import fr.centralesupelec.ptichatapp.Constants;
import fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection.SocketClientConnection;

public class SocketClient {

    private SocketClientConnection mScc;

    SocketClient(Context ctx) {
        try {
            Socket socket = new Socket(Constants.HOST_NAME, Constants.PORT);
            Log.i("SCc","😻 Client reached server");

            mScc = new SocketClientConnection(socket, ctx);

        } catch (IOException e) {
            Log.e("SCc","😿 Could not start the PtiChat Client Socket: " + e);
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (mScc == null) {
            Log.w("SCn", "🙀 mScc is null, dis gonna crash~~~");
        }
        mScc.sendMessage(message);
    }
}
