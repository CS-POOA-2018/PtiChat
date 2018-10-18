package fr.centralesupelec.ptichatapp;

import fr.centralesupelec.ptichatapp.PODS.User;

class Session {
    private static User user;

    // TODO use a session structure that will be the interface between data that servers sends, and the display
    // Store data when it is received, serve it when asked. Think about timing problems.

//    public static Boolean connect(String login, String password) {
//        // TODO : send it to a real backend
//        setUser(new User(login, login, "", true));
//        return true;
//    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Session.user = user;
    }
}
