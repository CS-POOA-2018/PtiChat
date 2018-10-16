package fr.centralesupelec.ptichatapp;

import java.util.Date;

public class Message {

    private User sender;
    private Chat chat;
    private String id;
    private Date date;
    private String content;
    private Boolean hasBeenRead;

    public Message(User sender, Chat chat, String id, Date date, String content, Boolean hasBeenRead) {
        setSender(sender);
        setChat(chat);
        setId(id);
        setDate(date);
        setContent(content);
        setHasBeenRead(hasBeenRead);
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getHasBeenRead() {
        return hasBeenRead;
    }

    public void setHasBeenRead(Boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

}
