package fr.centralesupelec.ptichatapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.centralesupelec.ptichatapp.PODS.Chat;
import fr.centralesupelec.ptichatapp.PODS.Message;
import fr.centralesupelec.ptichatapp.PODS.User;

public class JsonUtils {

    private static Chat chatJsonToChat(JSONObject chatJson) {
        try {
            return new Chat(
                    chatJson.getString("chatId"),
                    chatJson.getString("chatName")
            );
        } catch (JSONException e) {
            Log.e("JUe", "Could not make Chat from JSON: " + e.getMessage());
        }
        return null;
    }

    public static Message messageJsonToMessage(JSONObject messageJson) {
        try {
            return new Message(
                    messageJson.getString("messageId"),
                    messageJson.getString("content"),
                    Utils.stringToDate(messageJson.getString("date")),
                    messageJson.getString("senderId"),
                    messageJson.getString("chatId"),
                    messageJson.getBoolean("read")
            );
        } catch (JSONException e) {
            Log.e("JUe", "Could not make Message from JSON: " + e.getMessage());
        }
        return null;
    }

    private static User userJsonToUser(JSONObject userJson) {
        try {
            return new User(
                    userJson.getString("userId"),
                    userJson.getString("pseudo"),
                    userJson.optString("profilePicture"),
                    userJson.optString("status"),
                    userJson.getBoolean("isConnected")
            );
        } catch (JSONException e) {
            Log.e("JUe", "Could not make User from JSON: " + e.getMessage());
        }
        return null;
    }

    public static JSONObject userToJson(User user) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "editUser");
            toSend.put("userId", user.getId());
            toSend.put("pseudo", user.getPseudo());
            toSend.put("status", user.getStatus());
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject deleteUserJson(String userId) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "deleteUser");
            toSend.put("userId", userId);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject userInfoToNewUserJson(String login, String password) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "createNewUser");
            toSend.put("userId", login);
            toSend.put("password", password);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject createNewChat(String chatName, String[] userIds) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "createNewChat");
            toSend.put("chatName", chatName);
            toSend.put("users", new JSONArray(userIds));
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject deleteChatJson(String chatId) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "deleteChat");
            toSend.put("chatId", chatId);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForListOfUsers() {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getListOfUsers");
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForListOfChatMembers(String chatId) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getGroupMembers");
            toSend.put("chatId", chatId);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
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
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
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
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject askForPrivateMessages(String userId1, String userId2) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "getPrivateMessages");
            toSend.put("userId1", userId1);
            toSend.put("userId2", userId2);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static User loginAcceptanceJsonToUser(JSONObject json) {
        try {
            return userJsonToUser(json.getJSONObject("user"));
        } catch (JSONException e) {
            Log.e("JUe", "Could not parse loginAcceptance json: " + e.getMessage());
        }
        return null;
    }

    public static User[] listOfUsersJsonToUsers(JSONObject json) {
        List<User> userList = new ArrayList<>();
        try {
            JSONArray userJsonArray = json.getJSONArray("users");
            for (int i = 0; i < userJsonArray.length(); i++) {
                userList.add(userJsonToUser(userJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e("JUe", "Could not parse list of users json to users: " + e.getMessage());
        }
        return userList.toArray(new User[0]);
    }

    public static Chat[] listOfChatsJsonToUsers(JSONObject json) {
        List<Chat> chatList = new ArrayList<>();
        try {
            JSONArray chatJsonArray = json.getJSONArray("chats");
            for (int i = 0; i < chatJsonArray.length(); i++) {
                chatList.add(chatJsonToChat(chatJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e("JUe", "Could not parse list of chats json to chats: " + e.getMessage());
        }
        return chatList.toArray(new Chat[0]);
    }

    public static Message[] listOfMessagesJsonToMessages(JSONObject json) {
        List<Message> messageList = new ArrayList<>();
        try {
            JSONArray chatJsonArray = json.getJSONArray("messages");
            for (int i = 0; i < chatJsonArray.length(); i++) {
                messageList.add(messageJsonToMessage(chatJsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e("JUe", "Could not parse list of messages json to messages: " + e.getMessage());
        }
        return messageList.toArray(new Message[0]);
    }

    public static JSONObject messageToJson(Message message) {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("messageId", message.getId());
            json.put("content", message.getContent());
            json.put("date", Utils.dateToString(message.getDate()));
            json.put("senderId", message.getSenderId());
            json.put("chatId", message.getChatId());
            json.put("read", message.isRead());
        } catch (JSONException e) {
            Log.e("JUe", "Could not transform message to Json: " + e.getMessage());
        }
        return json;
    }

    public static JSONObject sendNewMessageJson(Message message) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "sendNewMessage");
            toSend.put("message", messageToJson(message));
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject announceConnection(String userId, boolean connection) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "announceConnection");
            toSend.put("connection", connection);
            toSend.put("userId", userId);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }

    public static JSONObject justTextJSON(String text) {
        JSONObject toSend = null;
        try {
            toSend = new JSONObject();
            toSend.put("type", "justText");
            toSend.put("content", text);
        } catch (JSONException e) {
            Log.e("JUe", "Could not make JSON: " + e.getMessage());
        }
        return toSend;
    }
}
