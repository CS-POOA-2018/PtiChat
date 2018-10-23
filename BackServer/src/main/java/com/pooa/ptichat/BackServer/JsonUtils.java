package com.pooa.ptichat.BackServer;

import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    public static JSONObject chatToJson(Chat chat) {
        JSONObject json = new JSONObject();
        json.put("chatId", chat.getId());
        json.put("chatName", chat.getName());
        return json;
    }

    public static JSONArray chatArrayToJsonArray(Chat[] chats) {
        JSONArray jsonArray = new JSONArray();
        for (Chat c : chats) {
            jsonArray.put(chatToJson(c));
        }
        return jsonArray;
    }

    public static JSONObject messageToJson(Message message) {
        JSONObject json = new JSONObject();
        json.put("messageId", message.getId());
        json.put("content", message.getContent());
        json.put("date", Utils.dateToString(message.getDate()));
        json.put("senderId", message.getSenderId());
        json.put("chatId", message.getChatId());
        json.put("read", message.isRead());
        return json;
    }

    public static JSONArray messageArrayToJsonArray(Message[] messages) {
        JSONArray jsonArray = new JSONArray();
        for (Message m : messages) {
            jsonArray.put(messageToJson(m));
        }
        return jsonArray;
    }

    public static JSONObject userToJson(User user) {
        JSONObject json = new JSONObject();
        json.put("userId", user.getId());
        json.put("pseudo", user.getPseudo());
        json.put("profilePicture", user.getProfilePicture());
        json.put("status", user.getStatus());
        json.put("isConnected", user.isConnected());
        return json;
    }

    public static JSONArray userArrayToJsonArray(User[] users) {
        JSONArray jsonArray = new JSONArray();
        for (User u : users) {
            jsonArray.put(userToJson(u));
        }
        return jsonArray;
    }

    public static JSONObject justTextJSON(String text) {
        JSONObject json = new JSONObject();
        json.put("type", "justText");
        json.put("content", text);
        return json;
    }

    public static JSONObject loginAcceptanceJSON(User user, boolean accepted, String message) {
        JSONObject json = new JSONObject();
        json.put("type", "loginAcceptance");
        json.put("user", (user != null) ? userToJson(user) : "");
        json.put("value", accepted);
        json.put("message", message);
        return json;
    }

    public static JSONObject editAcceptance(User user, boolean accepted, String message) {
        JSONObject json = new JSONObject();
        json.put("type", "editAcceptance");
        json.put("user", (user != null) ? userToJson(user) : "");
        json.put("value", accepted);
        json.put("message", message);
        return json;
    }

    public static JSONObject sendListOfChatsJson(String userId, Chat[] chats) {
        JSONObject json = new JSONObject();
        json.put("type", "listOfChats");
        json.put("userId", userId);
        json.put("chats", chatArrayToJsonArray(chats));
        return json;
    }

    public static JSONObject sendListOfUsersJson(User[] users) {
        JSONObject json = new JSONObject();
        json.put("type", "listOfUsers");
        json.put("users", userArrayToJsonArray(users));
        return json;
    }

    public static JSONObject sendListOfMessagesJson(String chatId, Message[] messages) {
        JSONObject json = new JSONObject();
        json.put("type", "listMessagesChat");
        json.put("chatId", chatId);
        json.put("messages", messageArrayToJsonArray(messages));
        return json;
    }

    public static JSONObject sendNewMessageInChat(String chatId, Message message) {
        JSONObject json = new JSONObject();
        json.put("type", "newMessageInChat");
        json.put("chatId", chatId);
        json.put("message", messageToJson(message));
        return json;
    }

    public static Chat jsonToChat(JSONObject json) {
        Chat c = new Chat(json.getString("chatName"));
        JSONArray usersJsonArray = new JSONArray(json.getString("users"));
        List<String> userList = new ArrayList<>();
        for(int i = 0; i < usersJsonArray.length(); i++){
            userList.add(usersJsonArray.getJSONObject(i).getString("name"));
        }
        c.setUsers(userList);
        return c;
    }

    public static Message jsonToMessage(JSONObject json) {
        return new Message(json.getString("messageId"), json.getString("content"), json.getString("senderId"), json.getString("chatId"));
    }

    public static User jsonToNewUser(JSONObject json) {
        return new User(json.getString("userId"), json.getString("password"), null);
    }

    public static User jsonToUser(JSONObject json) {
        return new User(json.getString("userId"), null, json.getString("pseudo"), null, json.getString("status"));
    }
}
