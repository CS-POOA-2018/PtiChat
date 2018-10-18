package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.JsonUtils;
import com.pooa.ptichat.BackServer.PODS.User;
import com.pooa.ptichat.BackServer.Storage.IStorage;
import com.pooa.ptichat.BackServer.StorageSingleton;
import org.json.JSONException;
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

    private void sendMessage(String message) {
        mOut.println(message);
        mOut.flush();
    }

    private void sendMessage(JSONObject json) {
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

                try {
                    // TODO obviously move that, in a separate Thread (or one per different handler), somewhere clean
                    JSONObject json = new JSONObject(messageIn);
                    String messageType = json.getString("type");

                    if ("createNewUser".equals(messageType)) {
                        User user = JsonUtils.jsonToUser(json);
                        String userId = user.getId();
                        String userPassword = user.getPassword();

                        IStorage storage = StorageSingleton.getInstance().getStorage();
                        boolean foundLogin = false;
                        boolean validCredentials = false;

                        User userMatch = storage.getUser(userId);

                        if (userMatch != null) {
                            foundLogin = true;
                            if (userPassword.equals(userMatch.getPassword())) {
                                validCredentials = true;
                            }
                        }
                        if (validCredentials) {
                            System.out.println("😺 User " + userId + " exists and user gave valid credentials");
                            sendMessage(JsonUtils.loginAcceptanceJSON(userMatch, true, ""));
                        } else if (foundLogin) {
                            System.out.println("🙀 User " + userId + " exists but user gave invalid credentials");
                            sendMessage(JsonUtils.loginAcceptanceJSON(userMatch, false, "Invalid Credentials"));
                        } else {
                            if (userId.length() >= 3) {
                                System.out.println("😻 User " + userId + " does not exist, creating it");
                                user.setPseudo(userId);
                                storage.addUser(user);
                                sendMessage(JsonUtils.loginAcceptanceJSON(user, true, ""));
                            } else {
                                sendMessage(JsonUtils.loginAcceptanceJSON(null, false, "Login too short"));
                            }
                        }
                    } else if ("getListOfUsers".equals(messageType)) {
                        StorageSingleton.getInstance().getConnectionsManager().checkConnectedUsers();
                        IStorage storage = StorageSingleton.getInstance().getStorage();
                        sendMessage(JsonUtils.sendListOfUsersJson(storage.listUsers()));

                    } else if ("getListOfChats".equals(messageType)) {
                        String userId = json.getString("userId");
                        IStorage storage = StorageSingleton.getInstance().getStorage();
                        sendMessage(JsonUtils.sendListOfChatsJson(userId, storage.listChatsOfUser(userId)));
                    }

                } catch (JSONException e) {
                    System.out.println("Could not parse message as JSON");
                }
            }
        } catch (IOException e) {
            System.out.println("😿 Client disconnected");
        }
    }
}
