package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.JsonUtils;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;
import com.pooa.ptichat.BackServer.Storage.IStorage;
import com.pooa.ptichat.BackServer.StorageSingleton;
import org.json.JSONException;
import org.json.JSONObject;

public class ReceivedMessageHandler implements Runnable {
    private String mMessage;
    private SocketServerConnection mSocketServerConnection;

    ReceivedMessageHandler(String message, SocketServerConnection socketServerConnection) {
        mMessage = message;
        mSocketServerConnection = socketServerConnection;
    }

    @Override
    public void run() {
        try {
            JSONObject json = new JSONObject(mMessage);
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
                    mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(userMatch, true, ""));
                } else if (foundLogin) {
                    System.out.println("🙀 User " + userId + " exists but user gave invalid credentials");
                    mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(userMatch, false, "Invalid Credentials"));
                } else {
                    if (userId.length() >= 3) {
                        System.out.println("😻 User " + userId + " does not exist, creating it");
                        user.setPseudo(userId);
                        storage.addUser(user);
                        mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(user, true, ""));
                    } else {
                        mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(null, false, "Login too short"));
                    }
                }
            } else if ("getListOfUsers".equals(messageType)) {
                StorageSingleton.getInstance().getConnectionsManager().checkConnectedUsers();
                IStorage storage = StorageSingleton.getInstance().getStorage();
                mSocketServerConnection.sendMessage(JsonUtils.sendListOfUsersJson(storage.listUsers()));

            } else if ("getListOfChats".equals(messageType)) {
                String userId = json.getString("userId");
                IStorage storage = StorageSingleton.getInstance().getStorage();
                mSocketServerConnection.sendMessage(JsonUtils.sendListOfChatsJson(userId, storage.listChatsOfUser(userId)));

            } else if ("getListOfMessages".equals(messageType)) {
                String chatId = json.getString("chatId");
                IStorage storage = StorageSingleton.getInstance().getStorage();
                mSocketServerConnection.sendMessage(JsonUtils.sendListOfMessagesJson(chatId, storage.listAllMessages(chatId)));

            } else if ("sendNewMessage".equals(messageType)) {
                Message newMessage = JsonUtils.jsonToMessage(json.getJSONObject("message"));
                IStorage storage = StorageSingleton.getInstance().getStorage();
                storage.addMessage(newMessage);
                // TODO notify all other people in chet

                mSocketServerConnection.sendMessage(JsonUtils.sendNewMessageInChat(newMessage.getChatId(), newMessage));
            }

        } catch (JSONException e) {
            System.out.println("Could not parse message as JSON");
        }

    }
}
