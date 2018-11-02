package com.pooa.ptichat.BackServer;

import com.pooa.ptichat.BackServer.NativeSocketServer.SocketServer;

public class BackServerStart {

    public static void main(String[] args) {
        SocketServer cs = new SocketServer();
        cs.startServer();
    }
}
