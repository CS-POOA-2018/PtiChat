package com.pooa.ptichat.BackServer.Storage;

import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;

import java.util.Arrays;
import java.util.Date;

public class DoStuffInStorage {
    public static void main(String[] args) {
        IStorage storage = new SqliteStorage();  // default db
        Chat[] chats = storage.listChats();
        Message[] messages = new Message[0];
        if (chats.length > 0) messages = storage.listMessages(chats[0].getId(), 3);
        User[] users = storage.listUsers();

        System.out.println("--- Users ---");
        System.out.println(Arrays.toString(users));

        System.out.println("\n--- Chats ---");
        System.out.println(Arrays.toString(chats));

        System.out.println("\n--- Messages ---");
        System.out.println(Arrays.toString(messages));

        System.out.println("\n==========\n");

        // Operations on the DataBase //

        storage.removeUser("plop");

        User u0 = storage.getUser("plop");
        if (u0 == null) {
            u0 = new User("plop", "plop", "Mr. Plop");
            u0.setStatus("Just plopping");
            storage.addUser(u0);
        }

        storage.removeUser("nope");

        User u1 = storage.getUser("nope");
        if (u1 == null) {
            u1 = new User("nope", "nope", "NopeNope");
            u1.setStatus("The void is eternal");
            storage.addUser(u1);
        }

        storage.removeUser("wah");

        User u2 = storage.getUser("wah");
        if (u2 == null) {
            u2 = new User("wah", "wah", "Waluigi");
            u2.setStatus("Waluigi time !");
            storage.addUser(u2);
        }

        storage.removeChat("be6c3fee-0f60-47ef-a595-3b076e6961e2");
        Chat c0 = new Chat("Default Chat");
        storage.addChat(c0);

        storage.userJoinsChat(u0.getId(), c0.getId());
        storage.userJoinsChat(u1.getId(), c0.getId());
        storage.userJoinsChat(u2.getId(), c0.getId());

        System.out.println("\n--- Chats ---");
        chats = storage.listChats();
        System.out.println(Arrays.toString(chats));

        Message m0 = new Message("Heyyy", "wah", c0.getId());
        Message m1 = new Message("Le Backend nous a envoyé du mockup :O", "wah", c0.getId());
        Message m2 = new Message("C'est dingue !", "nope", c0.getId());
        Message m3 = new Message("Teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeest XD", "nope", c0.getId());

        storage.addMessage(m0);
        storage.addMessage(m1);
        storage.addMessage(m2);
        storage.addMessage(m3);
    }
}
