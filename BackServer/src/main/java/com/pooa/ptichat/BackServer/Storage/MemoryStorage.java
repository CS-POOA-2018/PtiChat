package com.pooa.ptichat.BackServer.Storage;

import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryStorage implements IStorage {

    private Map<String, Chat> mChatData = new HashMap<>();
    private Map<String, Message> mMessageData = new HashMap<>();
    private Map<String, User> mUserData = new HashMap<>();

    @Override
    public void addChat(Chat chat) {
        mChatData.put(chat.getId(), chat);
    }

    @Override
    public void editChat(Chat chat) {
        mChatData.put(chat.getId(), chat);
    }

    @Override
    public Chat getChat(String chatId) {
        return mChatData.get(chatId);
    }

    @Override
    public void removeChat(String chatId) {
        mChatData.remove(chatId);
    }

    @Override
    public Chat[] listChats() {
        return mChatData.values().toArray(new Chat[0]);
    }

    @Override
    public Chat[] listChatsOfUser(String userId) {
        List<Chat> list = new ArrayList<>();
        for (Chat c : mChatData.values()) {
            if (c.getUsers().contains(userId)) {  // Not efficient!
                list.add(c);
            }
        }
        return list.toArray(new Chat[0]);
    }

    @Override
    public void addMessage(Message message) {
        mMessageData.put(message.getId(), message);
        mChatData.get(message.getChatId()).getMessages().add(message.getId());
    }

    @Override
    public Message[] listMessages(String chatId, int limit) {
        Message[] data = listAllMessages(chatId);
        int n = data.length;
        return Arrays.copyOfRange(data, Math.max(0, n - limit), limit);
    }

    @Override
    public Message[] listAllMessages(String chatId) {
        return mMessageData.values().toArray(new Message[0]);
    }

    @Override
    public void addUser(User user) {
        mUserData.put(user.getId(), user);
    }

    @Override
    public void editUser(User user) {
        mUserData.put(user.getId(), user);
    }

    @Override
    public User getUser(String userId) {
        return mUserData.get(userId);
    }

    @Override
    public void removeUser(String userId) {
        mUserData.remove(userId);
    }

    @Override
    public User[] listUsers() {
        return mUserData.values().toArray(new User[0]);
    }

    @Override
    public String[] listUserIdsInChat(String chatId) {
        return mChatData.get(chatId).getUsers().toArray(new String[0]);
    }

    @Override
    public User[] listUsersInChat(String chatId) {
        String[] userIds = mChatData.get(chatId).getUsers().toArray(new String[0]);
        User[] users = new User[userIds.length];
        for (int i = 0; i < userIds.length; i++) {
            users[i] = getUser(userIds[i]);
        }
        return users;
    }

    @Override
    public void userJoinsChat(String userId, String chatId) {
        mChatData.get(chatId).getUsers().add(userId);
    }
}
