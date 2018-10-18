package fr.centralesupelec.ptichatapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.User;

public class JsonUtils {
    // jsonToListOfChats
    // jsonToListOfMessages
    // jsonToListOfUsers

    public static JSONObject userInfoToNewUserJson(String login, String password) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "createNewUser");
            toSend.put("userId", login);
            toSend.put("password", password);
        } catch (JSONException e) {
            Log.e("LAc", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForListOfUsers() {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getListOfUsers");
        } catch (JSONException e) {
            Log.e("LAc", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForListOfChats(String userId) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getListOfChats");
            toSend.put("userId", userId);
        } catch (JSONException e) {
            Log.e("LAc", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForListOfMessages(String chatId) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getListOfMessages");
            toSend.put("chatId", chatId);
        } catch (JSONException e) {
            Log.e("LAc", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static User loginAcceptanceJsonToUser(JSONObject json) {
        User user = null;
        try {
            user = new User(
                    json.getString("userId"),
                    json.getString("pseudo"),
                    json.optString("profilePicture"),
                    json.optString("status"),
                    json.getBoolean("isConnected")
            );
        } catch (JSONException e) {
            Log.e("JUu", "Could not parse login acceptance Json to User: " + e.getMessage());
        }
        return user;
    }


//    public static Chat jsonToChat(JSONObject json) {
//        Chat c = new Chat(json.getString("chatName"));
//        JSONArray usersJsonArray = new JSONArray(json.getString("users"));
//        List<String> userList = new ArrayList<>();
//        for(int i = 0; i < usersJsonArray.length(); i++){
//            userList.add(usersJsonArray.getJSONObject(i).getString("name"));
//        }
//        c.setUsers(userList);
//        return c;
//    }
//
//    public static Message jsonToMessage(JSONObject json) {
//        return new Message(json.getString("content"), json.getString("senderId"), json.getString("chatId"));
//    }
//
//    public static User jsonToUser(JSONObject json) {
//        return new User(json.getString("userId"), json.getString("password"), null);
//    }
}
