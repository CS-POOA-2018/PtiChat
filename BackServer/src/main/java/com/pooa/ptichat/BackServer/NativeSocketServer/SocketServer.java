package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT, 50);
            System.out.println("😼 PtiChat socket server open on port " + Constants.SERVER_PORT);

            while (true) {  // Always accept new clients while running
                Socket socket = serverSocket.accept();
                System.out.println("😺 Got connection to the socket server: " + socket);

                Thread t = new Thread(new SocketServerConnection(socket));
                t.start();
            }
        } catch (IOException e) {
            System.out.println("🆘 Could not start the PtiChat Socket Server: " + e);
        }
    }
}
