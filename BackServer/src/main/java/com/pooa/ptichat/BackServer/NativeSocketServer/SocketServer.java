package com.pooa.ptichat.BackServer.NativeSocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static final int PORT = 8059;

    private void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT, 50);
            System.out.println("😼 PtiChat socket server open on port " + PORT);

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

    public static void main(String[] args) {
        SocketServer cs = new SocketServer();
        cs.startServer();
    }
}
