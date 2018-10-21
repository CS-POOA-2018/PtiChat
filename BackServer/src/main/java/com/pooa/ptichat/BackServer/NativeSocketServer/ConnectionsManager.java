package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.PODS.User;
import com.pooa.ptichat.BackServer.Storage.IStorage;
import com.pooa.ptichat.BackServer.StorageSingleton;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConnectionsManager {

    // Store the active connections in some data structure. It will be used to know who is connected,
    // and to send them messages if their are in one updated chat (or when someone becomes connected,...)

    private Map<SocketServerConnection, String> mActiveSocketsToUserId = new HashMap<>();
    private Map<String, SocketServerConnection> mUserIdToActiveSockets = new HashMap<>();

    public void registerSocket(SocketServerConnection scc) {
//        System.out.println("CM: registering socket " + scc);
        mActiveSocketsToUserId.put(scc, "");
        mUserIdToActiveSockets.put("", scc);
    }

    public void registerUserInSocket(SocketServerConnection scc, String userId) {
//        System.out.println("CM: registering socket " + scc + ", with user " + userId);
        mActiveSocketsToUserId.put(scc, userId);
        mUserIdToActiveSockets.put(userId, scc);
    }

    public void unregisterSocket(SocketServerConnection scc) {
        String userId = mActiveSocketsToUserId.get(scc);
//        System.out.println("CM: unregistering socket " + scc + ", was assigned to " + userId);
        mActiveSocketsToUserId.remove(scc);
        mUserIdToActiveSockets.remove(userId);
    }

    private boolean isUserConnected(String userId) {
        return mActiveSocketsToUserId.containsValue(userId);
    }

    /** Will update the User objects with their actual connection state (isConnected <- true or false) */
    public void checkConnectedUsers(User[] users) {
        for (User u : users) {
            u.setConnected(isUserConnected(u.getId()));
        }
    }

    public void sendMessageToAllConnectedUsers(JSONObject json) {
        for (User u : StorageSingleton.getInstance().getStorage().listUsers()) {
            if (isUserConnected(u.getId())) {
                SocketServerConnection userScc = mUserIdToActiveSockets.get(u.getId());
                userScc.sendMessage(json);
            }
        }
    }

    public void sendMessageToAllConnectedUsersInChat(String chatId, JSONObject json) {
        IStorage storage = StorageSingleton.getInstance().getStorage();
        for (String userId : storage.listUserIdsInChat(chatId)) {
            if (isUserConnected(userId)) {
                SocketServerConnection userScc = mUserIdToActiveSockets.get(userId);
                userScc.sendMessage(json);
            }
        }
    }
}
