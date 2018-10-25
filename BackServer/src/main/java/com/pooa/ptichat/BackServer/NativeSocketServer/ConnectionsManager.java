package com.pooa.ptichat.BackServer.NativeSocketServer;

import com.pooa.ptichat.BackServer.PODS.User;
import com.pooa.ptichat.BackServer.Storage.IStorage;
import com.pooa.ptichat.BackServer.StorageSingleton;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionsManager {

    // Store the active connections in some data structure. It will be used to know who is connected,
    // and to send them messages if their are in one updated chat (or when someone becomes connected,...)

    private Map<SocketServerConnection, String> mActiveSocketsToUserId = new HashMap<>();
    private Map<String, List<SocketServerConnection>> mUserIdToActiveSockets = new HashMap<>();

    public void registerSocket(SocketServerConnection scc) {
        registerUserInSocket(scc, "");
    }

    public void registerUserInSocket(SocketServerConnection scc, String userId) {
//        System.out.println("CM: registering socket " + scc + ", with user " + userId);
        mActiveSocketsToUserId.put(scc, userId);

        mUserIdToActiveSockets.computeIfAbsent(userId, k -> new ArrayList<>());
        if (!mUserIdToActiveSockets.get(userId).contains(scc)) mUserIdToActiveSockets.get(userId).add(scc);
    }

    public void unregisterUserInSocket(SocketServerConnection scc, String userId) {
//        System.out.println("CM: unregistering socket " + scc + " for user " + userId);
        mActiveSocketsToUserId.put(scc, "");

        if (mUserIdToActiveSockets.get(userId) != null) {
            mUserIdToActiveSockets.get(userId).remove(scc);
            if (mUserIdToActiveSockets.get(userId).size() == 0) {
                mUserIdToActiveSockets.remove(userId);
            }
        }
    }

    public void unregisterSocket(SocketServerConnection scc) {
        String userId = mActiveSocketsToUserId.get(scc);
//        System.out.println("CM: unregistering socket " + scc + ", was assigned to " + userId);

        unregisterUserInSocket(scc, userId);
        mActiveSocketsToUserId.remove(scc);
    }

    public String getUserOfSocket(SocketServerConnection scc) {
        return mActiveSocketsToUserId.get(scc);
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
        for (SocketServerConnection userScc : mActiveSocketsToUserId.keySet()) {
            if (!mActiveSocketsToUserId.get(userScc).isEmpty()) {
                userScc.sendMessage(json);
            }
        }
    }

    public void sendMessageToAllConnectedUsersInChat(String chatId, JSONObject json) {
        IStorage storage = StorageSingleton.getInstance().getStorage();
        for (String userId : storage.listUserIdsInChat(chatId)) {
            if (isUserConnected(userId)) {
                for (SocketServerConnection userScc : mUserIdToActiveSockets.get(userId)) {
                    userScc.sendMessage(json);
                }
            }
        }
    }
}
