package com.pooa.ptichat.BackServer.Storage;

import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public abstract class StorageTests {

    private IStorage mStorage;

    public abstract IStorage createInstance();

    @Before
    public void init() {
        mStorage = createInstance();
    }

    @Test
    public void testAddChat() {
        Chat c0 = new Chat("TestChat");
        String c0id = c0.getId();
        mStorage.addChat(c0);

        Chat[] chatArray = mStorage.listChats();
        assert Arrays.stream(chatArray).anyMatch(c -> c0id.equals(c.getId()));
        Chat queriedChat1 = mStorage.getChat(c0id);
        assert queriedChat1 != null;
        assert queriedChat1.getId().equals(c0.getId());
        assert queriedChat1.getName().equals(c0.getName());

        mStorage.removeChat(c0.getId());
        Chat[] chatArray2 = mStorage.listChats();
        assert Arrays.stream(chatArray2).noneMatch(c -> c0id.equals(c.getId()));
        Chat queriedChat2 = mStorage.getChat(c0id);
        assert queriedChat2 == null;
    }

    @Test
    public void testAddUser() {
        User u0 = new User("TestUser", "password", "John Test");
        mStorage.addUser(u0);

        User[] userArray = mStorage.listUsers();
        assert Arrays.stream(userArray).anyMatch(u -> "TestUser".equals(u.getId()));
        User queriedUser1 = mStorage.getUser("TestUser");
        assert queriedUser1 != null;
        assert queriedUser1.getId().equals(u0.getId());
        assert queriedUser1.getPassword().equals(u0.getPassword());

        mStorage.removeUser(u0.getId());
        User[] userArray2 = mStorage.listUsers();
        assert Arrays.stream(userArray2).noneMatch(u -> "TestUser".equals(u.getId()));
        User queriedUser2 = mStorage.getUser("TestUser");
        assert queriedUser2 == null;
    }

    @Test
    public void testAddMessage() {
        Chat c0 = new Chat("TestChat");

        User u0 = new User("TestUser", "password", "John Test");

        mStorage.addChat(c0);
        mStorage.addUser(u0);
        mStorage.userJoinsChat(u0.getId(), c0.getId());

        Message m0 = new Message("Hello!", u0.getId(), c0.getId());
        mStorage.addMessage(m0);

        String m0id = m0.getId();

        Message[] messageArray = mStorage.listMessages(c0.getId(), 50);
        assert Arrays.stream(messageArray).anyMatch(m -> m0id.equals(m.getId()));
        assert Arrays.stream(messageArray).anyMatch(m -> "Hello!".equals(m.getContent()));

        System.out.println(Arrays.toString(mStorage.listChats()));

        mStorage.removeUser(u0.getId());
        mStorage.removeChat(c0.getId());
    }

    @Test
    public void testListChats() {
        Chat c0 = new Chat("TestChat00");
        Chat c1 = new Chat("TestChat01");

        User u0 = new User("TestUser", "password", "John Test");

        mStorage.addChat(c0);
        mStorage.addChat(c1);
        String c0id = c0.getId();
        String c1id = c1.getId();

        mStorage.addUser(u0);
        mStorage.userJoinsChat(u0.getId(), c0.getId());

        Chat[] allChats = mStorage.listChats();
        assert Arrays.stream(allChats).anyMatch(c -> c0id.equals(c.getId()));
        assert Arrays.stream(allChats).anyMatch(c -> c1id.equals(c.getId()));

        Chat[] chatsOfUser = mStorage.listChatsOfUser(u0.getId());
        assert Arrays.stream(chatsOfUser).anyMatch(c -> c0id.equals(c.getId()));
        assert Arrays.stream(chatsOfUser).noneMatch(c -> c1id.equals(c.getId()));

        String[] userIdsOfChat = mStorage.listUserIdsInChat(c0id);
        assert Arrays.asList(userIdsOfChat).contains("TestUser");

        mStorage.removeUser(u0.getId());
        mStorage.removeChat(c0.getId());
        mStorage.removeChat(c1.getId());
    }
}
