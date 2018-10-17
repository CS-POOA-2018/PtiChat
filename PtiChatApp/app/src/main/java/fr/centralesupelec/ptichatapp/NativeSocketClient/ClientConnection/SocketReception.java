package fr.centralesupelec.ptichatapp.NativeSocketClient.ClientConnection;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import fr.centralesupelec.ptichatapp.Constants;
import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.NativeSocketClient.SocketSingleton;

public class SocketReception implements Runnable {

    private Context mCtx;
    private Socket mSocket;

    private BufferedReader mIn;

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
            e.printStackTrace();
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

            while (true) {  // Always listening until quitting is requested
                messageIn = receiveMessage();

                if (messageIn == null) break;

                // Send the new incoming message to the other classes
                broadcastNewMessage(messageIn);

                if ("HLO".equals(messageIn)) {
                    Log.i("CCh", "😻 Client received HELLO! \\o/. Responding with HELLO");
                    SendMessageTask.sendMessageAsync(mCtx, "HLO");
                }

                if ("QUT".equals(messageIn)) {
                    Log.i("CCq", "😿 QUIT requested, quitting");
                    break;
                }
            }
            Log.w("CCk", "😿 Connection with the server broke up...");
            SocketSingleton.getInstance(mCtx).setConnected(false);
            SocketSingleton.renewSocketClient();

        } catch (IOException e) {
            Log.w("CCq", "😿 Server does not respond...");
        }
    }
}
