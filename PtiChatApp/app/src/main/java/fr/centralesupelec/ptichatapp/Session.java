package fr.centralesupelec.ptichatapp;

import fr.centralesupelec.ptichatapp.PODS.User;

class Session {
    private static User user;

    public static Boolean connect(String login, String password) {
        // TODO : send it to a real backend
        setUser(new User(login, login, "", "", true));
        return true;
    }

    public static User getUser() {
        return user;
    }

    private static void setUser(User user) {
        Session.user = user;
    }
}
