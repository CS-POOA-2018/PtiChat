package fr.centralesupelec.ptichatapp;

public class User {

    private String pseudo;
    private String id;
    private String status;
    private Boolean isConnected;

    public User(String pseudo, String id, String status, Boolean isConnected) {
        setPseudo(pseudo);
        setId(id);
        setStatus(status);
        setConnected(isConnected);
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean isConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }
}
