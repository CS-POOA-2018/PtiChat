package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import fr.centralesupelec.ptichatapp.Constants;
import fr.centralesupelec.ptichatapp.JsonUtils;
import fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection.SocketClientConnection;
import fr.centralesupelec.ptichatapp.Session;

public class SocketClient {

    public SocketClientConnection mScc;
    private Context mCtx;

    SocketClient(Context ctx) {
        try {
            Socket socket = new Socket(Constants.HOST_NAME, Constants.PORT);
            Log.i("SCc","😻 Client reached server");

            mCtx = ctx;
            mScc = new SocketClientConnection(socket, ctx);

        } catch (IOException e) {
            Log.e("SCc","😿 Could not start the PtiChat Client Socket: " + e.getMessage());
        }
    }

    public boolean renewSocketClient() {
        try {
            Socket socket = new Socket(Constants.HOST_NAME, Constants.PORT);
            Log.i("SCc","😻 Client reached server again");

            mScc = new SocketClientConnection(socket, mCtx);
            if (Session.getUserId() != null) SendMessageTask.sendMessageAsync(mCtx, JsonUtils.announceConnection(Session.getUserId()));
            return true;
        } catch (IOException e) {
            Log.e("SCc","😿 Could not renew the PtiChat Client Socket: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (mScc == null) {
            Log.w("SCn", "🙀 mScc is null, dis gonna crash~~~");
        }
        mScc.sendMessage(message);
    }
}
