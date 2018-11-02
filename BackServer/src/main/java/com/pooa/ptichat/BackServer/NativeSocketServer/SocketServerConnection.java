package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.JsonUtils;
import com.pooa.ptichat.BackServer.StorageSingleton;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketServerConnection implements Runnable {

    private Socket mSocket;

    private BufferedReader mIn;
    private PrintWriter mOut;

    SocketServerConnection(Socket socket) {
        mSocket = socket;
    }

    void sendMessage(String message) {
        System.out.println("📧 Sending message: " + ((message.length() <= 180) ? message : message.substring(0, 176) + " ···"));
        mOut.println(message);
        mOut.flush();
    }

    void sendMessage(JSONObject json) {
        sendMessage(json.toString());
    }

    private String receiveMessage() {
        try {
            String messageIn = mIn.readLine();
            if (messageIn == null) {
                System.out.println("❕ Received empty message");
            } else {
                System.out.println("📩 Received message: " + messageIn);
            }
            return messageIn;
        } catch (IOException e) {
            System.out.println("🚪 Could not read inbound message: " + e.getMessage());
            return null;
        }
    }

    public void run() {
        try {
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOut = new PrintWriter(mSocket.getOutputStream());
            String messageIn;
            System.out.println("😻 SocketServerConnection started");
            StorageSingleton.getInstance().getConnectionsManager().registerSocket(this);

            sendMessage(JsonUtils.justTextJSON("The server says hello to you ❤️"));

            while (true) {  // Always listening until quitting is requested
                messageIn = receiveMessage();

                if (messageIn == null) break;

                Thread handlerThread = new Thread(new ReceivedMessageHandler(messageIn, this));
                handlerThread.start();
            }
            System.out.println("😿 Client disconnected");

            ConnectionsManager cm = StorageSingleton.getInstance().getConnectionsManager();
            String userWhoWasConnected = cm.getUserOfSocket(this);
            cm.unregisterSocket(this);
            if (userWhoWasConnected != null) cm.sendMessageToAllConnectedUsers(JsonUtils.announceConnection(userWhoWasConnected, false));

        } catch (IOException e) {
            System.out.println("😿 Client disconnected due to error: " + e.getMessage());
        }
    }
}
