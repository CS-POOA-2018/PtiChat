package fr.centralesupelec.ptichatapp.PODS;

import java.util.Date;
import java.util.UUID;

public class Message {
    private String id;
    private String content;
    private Date date;
    private String senderId;
    private String chatId;
    private boolean read;

    /** Use this constructor when a new message is created (without uuid), it will give it a new uuid */
    public Message(String content, String senderId, String chatId) {
        this.content = content;
        this.senderId = senderId;
        this.chatId = chatId;

        this.date = null;
        this.id = UUID.randomUUID().toString();
        this.read = false;
    }

    /** Constructor when all message attributes are known (sent by BackServer) */
    public Message(String id, String content, Date date, String senderId, String chatId, boolean read) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.senderId = senderId;
        this.chatId = chatId;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
