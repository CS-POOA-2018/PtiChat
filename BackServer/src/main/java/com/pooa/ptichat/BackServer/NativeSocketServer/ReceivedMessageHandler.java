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

    private void acceptUserLogin(User user) {
        mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(user, true, ""));
        StorageSingleton.getInstance().getConnectionsManager().registerUserInSocket(mSocketServerConnection, user.getId());
    }

    private void rejectUserLogin(User user, String message) {
        mSocketServerConnection.sendMessage(JsonUtils.loginAcceptanceJSON(user, false, message));
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

                if (userId == null) {
                    rejectUserLogin(null, "Login is null");
                }

                IStorage storage = StorageSingleton.getInstance().getStorage();
                boolean foundLogin = false;
                boolean validCredentials = false;

                User userMatch = storage.getUser(userId);

                if (userMatch != null) {
                    foundLogin = true;
                    validCredentials = userPassword.equals(userMatch.getPassword());
                }

                if (validCredentials) {
                    System.out.println("😺 User " + userId + " exists and user gave valid credentials");
                    acceptUserLogin(userMatch);

                } else if (foundLogin) {
                    System.out.println("🙀 User " + userId + " exists but user gave invalid credentials");
                    rejectUserLogin(userMatch, "Invalid Credentials");

                } else {
                    if (userId.length() >= 3) {
                        System.out.println("😻 User " + userId + " does not exist, creating it");
                        user.setPseudo(userId);
                        storage.addUser(user);
                        acceptUserLogin(user);
                    } else {
                        rejectUserLogin(null, "Login too short");
                    }
                }

            } else if ("getListOfUsers".equals(messageType)) {
                IStorage storage = StorageSingleton.getInstance().getStorage();
                User[] allUsers = storage.listUsers();
                StorageSingleton.getInstance().getConnectionsManager().checkConnectedUsers(allUsers);
                mSocketServerConnection.sendMessage(JsonUtils.sendListOfUsersJson(allUsers));

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

                // Notify all other people in chat
                JSONObject toSend = JsonUtils.sendNewMessageInChat(newMessage.getChatId(), newMessage);
                StorageSingleton.getInstance().getConnectionsManager().sendMessageToAllConnectedUsersInChat(newMessage.getChatId(), toSend);

            } else if ("announceConnection".equals(messageType)) {
                String userId = json.getString("userId");
                StorageSingleton.getInstance().getConnectionsManager().registerUserInSocket(mSocketServerConnection, userId);

            } else if ("justText".equals(messageType)) {
                String content = json.getString("content");
                System.out.println("🔤 App sent justText message: " + content);
            }

        } catch (JSONException e) {
            System.out.println("🆘 Could not parse message as JSON");
        }

    }
}
