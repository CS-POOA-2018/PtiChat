package fr.centralesupelec.ptichatapp;

import fr.centralesupelec.ptichatapp.PODS.User;

/**
 * Serve as global variables, shared by all the classes. No real need for a singleton here
 */
public class Session {
    private static User user;

    public static User getUser() {
        return user;
    }

    public static String getUserId() {
        return (user == null) ? null : user.getId();
    }

    public static void setUser(User user) {
        Session.user = user;
    }
}
