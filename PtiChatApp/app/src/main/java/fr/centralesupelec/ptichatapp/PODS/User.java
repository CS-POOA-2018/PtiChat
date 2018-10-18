package fr.centralesupelec.ptichatapp.PODS;

public class User {
    private String id;
    private String password;
    private String pseudo;
    private String profilePicture;
    private String status;
    private boolean isConnected;

    /** Constructor when all user attributes are known (sent by BackServer) */
    public User(String id, String pseudo, String profilePicture, String status, boolean isConnected) {
        this.id = id;
        this.pseudo = pseudo;
        this.profilePicture = profilePicture;
        this.status = status;
        this.isConnected = isConnected;
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
