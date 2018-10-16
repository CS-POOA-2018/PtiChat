package com.pooa.ptichat.BackServer.PODS;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Chat {
    private String id;
    private String name;
    private List<String> users;
    private List<String> messages;

    /**
     * Creates a new chat, just by name (ex: user creates a new chat in frontend)
     * @param name Name of the chat
     */
    public Chat(String name) {
        this.name = name;

        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();

        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    /**
     * Creates a new chat object from existing chat
     * @param id ID of the chat
     * @param name Name of the chat
     */
    public Chat(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
