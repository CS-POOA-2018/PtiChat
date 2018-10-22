package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import fr.centralesupelec.ptichatapp.Constants;
import fr.centralesupelec.ptichatapp.NativeSocketClient.SocketSingleton;

class SocketReception implements Runnable {

    private Context mCtx;
    private Socket mSocket;

    private BufferedReader mIn;
    private boolean mClosed = false;

    SocketReception(Socket socket, Context ctx) {
        Log.d("SSc", "✨ Creating the SocketReception instance");
        mCtx = ctx;
        mSocket = socket;
    }

    private String receiveMessage() {
        try {
            String messageIn = mIn.readLine();
            if (messageIn == null) {
                Log.e("CCe", "❕ Received empty message");
            } else {
                Log.i("CCr", "📩 Received message: " + messageIn);
            }
            return messageIn;
        } catch (IOException e) {
            Log.w("SRr", "🚪 Could not read inbound message: " + e.getMessage());
            return null;
        }
    }

    private void broadcastNewMessage(String message) {
        Intent intent = new Intent(Constants.BROADCAST_NEW_MESSAGE);
        intent.putExtra("message", message);
        mCtx.sendBroadcast(intent);
    }

    public void run() {
        try {
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            String messageIn;

            while (!mClosed) {  // Always listening until connection is closed or broken
                messageIn = receiveMessage();

                if (messageIn == null) break;

                // Send the new incoming message to the other classes
                broadcastNewMessage(messageIn);
            }
            if (mClosed) {
                Log.i("CCk", "😿 Connection with the server was closed");
            } else {
                Log.w("CCk", "😿 Connection with the server broke up...");
                SocketSingleton.renewSocketClient();
            }

        } catch (IOException e) {
            Log.w("CCq", "😿 Server does not respond...");
        }
    }

    void close() {
        mClosed = true;
        try {
            mIn.close();
            mSocket.close();
        } catch (IOException e) {
            Log.w("SRc", "❗️Could not close mIn: " + e.getMessage());
        }
    }
}
