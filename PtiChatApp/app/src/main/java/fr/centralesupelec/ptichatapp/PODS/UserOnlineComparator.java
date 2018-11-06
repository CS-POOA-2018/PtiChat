package fr.centralesupelec.ptichatapp.PODS;

import java.util.Comparator;

public class UserOnlineComparator implements Comparator<User> {

    // Order users depending on their connection status then on their pseudo alphabetically
    @Override
    public int compare(User user1, User user2) {
        if (user1.isConnected() && !user2.isConnected()) {
            return -1;
        } else if (!user1.isConnected() && user2.isConnected()) {
            return 1;
        } else {
            return user1.getPseudo().compareTo(user2.getPseudo());
        }
    }

}
