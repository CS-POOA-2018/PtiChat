package com.pooa.ptichat.BackServer.PODS;

public class User {
    private String id;
    private String password;
    private String pseudo;
    private String profilePicture;
    private String status;
    private boolean isConnected;

    /** Use this constructor when creating a new user */
    public User(String id, String password, String pseudo) {
        this.id = id;
        this.password = password;
        this.pseudo = pseudo;

        this.profilePicture = null;
        this.status = null;
    }

    /** Constructor when all user attributes are known */
    public User(String id, String password, String pseudo, String profilePicture, String status) {
        this.id = id;
        this.password = password;
        this.pseudo = pseudo;
        this.profilePicture = profilePicture;
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", status='" + status + '\'' +
                ", isConnected=" + isConnected +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
