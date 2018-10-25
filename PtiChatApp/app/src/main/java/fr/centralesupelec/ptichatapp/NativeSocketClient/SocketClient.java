package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.net.Socket;

import fr.centralesupelec.ptichatapp.JsonUtils;
import fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection.SocketClientConnection;
import fr.centralesupelec.ptichatapp.Session;
import fr.centralesupelec.ptichatapp.Utils;

public class SocketClient {

    private Socket mSocket = null;
    private SocketClientConnection mScc = null;
    private Context mCtx;

    SocketClient(Context ctx) {
        mCtx = ctx;
        Pair<String, Integer> hostInfo = Utils.getHostInfo(mCtx);

        try {
            mSocket = new Socket(hostInfo.first, hostInfo.second);
            Log.i("SCc","😻 Client reached server");

            mScc = new SocketClientConnection(mSocket, mCtx);

        } catch (IOException e) {
            Log.e("SCc","😿 Could not start the PtiChat Client Socket: " + e.getMessage());
        }
    }

    private void close() {
        mScc.close();
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.w("SCc", "❗️Could not close mSocket: " + e.getMessage());
        }
    }

    public boolean renewSocketClient() {
        Pair<String, Integer> hostInfo = Utils.getHostInfo(mCtx);

        try {
            close();
            mSocket = new Socket(hostInfo.first, hostInfo.second);
            Log.i("SCc","😻 Client reached server again");

            mScc = new SocketClientConnection(mSocket, mCtx);
            if (Session.getUserId() != null) SendMessageTask.sendMessageAsync(mCtx, JsonUtils.announceConnection(Session.getUserId(), true));
            return true;
        } catch (IOException e) {
            Log.e("SCc","😿 Could not renew the PtiChat Client Socket: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (mScc == null) {
            Log.w("SCn", "🙀 Cannot send message, mScc is null");
            return;
        }
        mScc.sendMessage(message);
    }
}
