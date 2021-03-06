package com.pooa.ptichat.BackServer.Storage;

import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;

public interface IStorage {
    void addChat(Chat chat);
    void editChat(Chat chat);
    Chat getChat(String chatId);
    void removeChat(String chatId);
    Chat[] listChats();
    Chat[] listChatsOfUser(String userId);

    void addMessage(Message Message);
    Message[] listMessages(String chatId, int limit);
    Message[] listAllMessages(String chatId);

    void addUser(User user);
    void editUser(User user);
    User getUser(String userId);
    void removeUser(String userId);
    User[] listUsers();
    String[] listUserIdsInChat(String chatId);
    User[] listUsersInChat(String chatId);

    void userJoinsChat(String userId, String chatId);
}
