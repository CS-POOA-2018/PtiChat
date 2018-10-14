package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.StorageSingleton;

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

    private void sendMessage(String message) {
        mOut.println(message);
        mOut.flush();
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
            e.printStackTrace();
            return null;
        }
    }

    public void run() {
        try {
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOut = new PrintWriter(mSocket.getOutputStream());
            String messageIn;
            System.out.println("😻 SocketServerConnection started");

            System.out.println("😺 Sending HLO...");
            sendMessage("HLO");

            boolean saidHello = false;
            while (!saidHello) {
                messageIn = receiveMessage();

                if ("HLO".equals(messageIn)) {
                    saidHello = true;
                    System.out.println("😺 Server received HELLO! \\o/");
                } else {
                    System.out.println("😿 Client did not say HELLO ._.");
                }
            }

            sendMessage("The server says hello to you ❤️");

            while (true) {  // Always listening until quitting is requested
                messageIn = receiveMessage();

                if (messageIn == null) break;

                if ("QUT".equals(messageIn)) {
                    System.out.println("😿 QUIT requested, quitting");
                    break;
                }

                if ("PLP".equals(messageIn)) {
                    int responseNum = StorageSingleton.getInstance().getNextPlop();
                    System.out.println("👈 Plop #" + responseNum);
                    sendMessage("This was Plop #" + responseNum);
                }
            }

        } catch (IOException e) {
            System.out.println("😿 Client disconnected");
        }
    }
}
