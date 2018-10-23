package com.pooa.ptichat.BackServer.Storage;

import com.pooa.ptichat.BackServer.Constants;
import com.pooa.ptichat.BackServer.PODS.Chat;
import com.pooa.ptichat.BackServer.PODS.Message;
import com.pooa.ptichat.BackServer.PODS.User;
import com.pooa.ptichat.BackServer.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SqliteStorage implements IStorage {

    private static String url = "jdbc:sqlite:" + Constants.SQLITE_PATH;
    private boolean isReady = false;

    public SqliteStorage() {
        getReady();
    }

    public SqliteStorage(String urlOverride) {
        url = urlOverride;
        getReady();
    }

    private static void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("🗄 Connected to database");
//                DatabaseMetaData meta = conn.getMetaData();
//                System.out.println("The driver name is " + meta.getDriverName());
//                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println("🆘 createNewDatabase failed: " + e.getMessage());
        }
    }

    private static void createNewTable() {
        // SQL statement for creating new tables
        String mk_chats = "CREATE TABLE IF NOT EXISTS chats ("
                + "	chatId text PRIMARY KEY,"
                + "	name text NOT NULL,"
                + " isPrivate boolean NOT NULL"
                + ");";

        String mk_messages = "CREATE TABLE IF NOT EXISTS messages ("
                + "	messageId text PRIMARY KEY,"
                + "	content text NOT NULL,"
                + "	sendDate text,"
                + "	senderId text,"
                + "	chatId text,"
                + "	read boolean NOT NULL"
                + ");";

        String mk_users = "CREATE TABLE IF NOT EXISTS users ("
                + "	userId text PRIMARY KEY,"
                + "	password text NOT NULL,"
                + "	pseudo text NOT NULL,"
                + "	profilePicture text,"
                + "	status text"
                + ");";

        String mk_userschats = "CREATE TABLE IF NOT EXISTS userschats ("
                + "	ucid integer PRIMARY KEY,"
                + "	userId text,"
                + "	chatId text"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create new tables
            stmt.execute(mk_chats);
            stmt.execute(mk_messages);
            stmt.execute(mk_users);
            stmt.execute(mk_userschats);
        } catch (SQLException e) {
            System.out.println("🆘 createNewTable failed: " + e.getMessage());
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("🆘 connect failed: " + e.getMessage());
        }
        return conn;
    }

    private void getReady() {
        if (isReady) return;
        createNewDatabase();
        createNewTable();
        isReady = true;
    }

    @Override
    public void addChat(Chat chat) {
//        System.out.println("Adding chat " + chat.getId());
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chats(chatId,name,isPrivate) VALUES(?,?,?)")) {
            pstmt.setString(1, chat.getId());
            pstmt.setString(2, chat.getName());
            pstmt.setBoolean(3, chat.isPrivate());
            // Chat is empty on creating, no adding of its list of users and messages
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 addChat failed: " + e.getMessage());
        }
    }

    @Override
    public Chat getChat(String chatId) {
        String sql = "SELECT chatId, name, isPrivate FROM chats WHERE chatId = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatId);
            ResultSet rs  = pstmt.executeQuery();

            if (rs.next()) {
                return new Chat(rs.getString("chatId"), rs.getString("name"), rs.getBoolean("isPrivate"));
            }
        } catch (SQLException e) {
            System.out.println("🆘 getChat failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void removeChat(String chatId) {
//        System.out.println("Removing chat " + chatId);
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM chats WHERE chatId = ?");
             PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM userschats WHERE chatId = ?");
             PreparedStatement pstmt3 = conn.prepareStatement("DELETE FROM messages WHERE chatId = ?")) {
            pstmt.setString(1, chatId);
            pstmt.executeUpdate();
            pstmt2.setString(1, chatId);
            pstmt2.executeUpdate();
            pstmt3.setString(1, chatId);
            pstmt3.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 removeChat failed: " + e.getMessage());
        }
    }

    @Override
    public Chat[] listChats() {
//        System.out.println("Listing chats");
        List<Chat> chatList = new ArrayList<>();

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery("SELECT chatId, name FROM chats WHERE isPrivate = false")){

            while (rs.next()) {
                Chat c = new Chat(rs.getString("chatId"), rs.getString("name"));
                // TODO add the chat users and messages ?
                chatList.add(c);
            }
        } catch (SQLException e) {
            System.out.println("🆘 listChats failed: " + e.getMessage());
        }
        return chatList.toArray(new Chat[0]);
    }

    @Override
    public Chat[] listChatsOfUser(String userId) {
//        System.out.println("Listing chats of user " + userId);
        String sql = "SELECT DISTINCT chats.chatId, name FROM chats INNER JOIN userschats "
                   + "ON chats.chatID = userschats.chatID WHERE isPrivate = false AND userId = ?";
        List<Chat> chatList = new ArrayList<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, userId);
            ResultSet rs  = pstmt.executeQuery();

            while (rs.next()) {
                Chat c = new Chat(rs.getString("chatId"), rs.getString("name"));
                chatList.add(c);
            }
        } catch (SQLException e) {
            System.out.println("🆘 listChatsOfUser failed: " + e.getMessage());
        }
        return chatList.toArray(new Chat[0]);
    }

    @Override
    public void addMessage(Message message) {
//        System.out.println("Adding message " + message.getId());
        String sql = "INSERT INTO messages(messageId,content,sendDate,senderId,chatId,read) VALUES(?,?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getId());
            pstmt.setString(2, message.getContent());
            pstmt.setString(3, Utils.dateToString(message.getDate()));
            pstmt.setString(4, message.getSenderId());
            pstmt.setString(5, message.getChatId());
            pstmt.setBoolean(6, message.isRead());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 addMessage failed: " + e.getMessage());
        }
    }

    private Message[] listMessagesAux(String sqlRequest) {
        List<Message> messageList = new ArrayList<>();

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sqlRequest)){

            while (rs.next()) {
                Message m = new Message(
                        rs.getString("messageId"),
                        rs.getString("content"),
                        Utils.stringToDate(rs.getString("sendDate")),
                        rs.getString("senderId"),
                        rs.getString("chatId"),
                        rs.getBoolean("read")
                );
                messageList.add(m);
            }
        } catch (SQLException e) {
            System.out.println("🆘 listMessages failed: " + e.getMessage());
        }
        return messageList.toArray(new Message[0]);
    }

    @Override
    public Message[] listMessages(String chatId, int limit) {
//        System.out.println("Listing messages of chat " + chatId + " (limit " + limit + ")");
        // TODO concat berk
        return listMessagesAux("SELECT messageId, content, sendDate, senderId, chatId, read FROM messages WHERE chatId = \"" + chatId + "\" LIMIT " + limit);
    }

    @Override
    public Message[] listAllMessages(String chatId) {
//        System.out.println("Listing messages of chat " + chatId);
        return listMessagesAux("SELECT messageId, content, sendDate, senderId, chatId, read FROM messages WHERE chatId = \"" + chatId + "\"");
    }

    @Override
    public void addUser(User user) {
//        System.out.println("Adding user " + user.getId());
        String sql = "INSERT INTO users(userId,password,pseudo,profilePicture,status) VALUES(?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getPseudo());
            pstmt.setString(4, user.getProfilePicture());
            pstmt.setString(5, user.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 addUser failed: " + e.getMessage());
        }
    }

    @Override
    public User getUser(String userId) {
        String sql = "SELECT userId,password,pseudo,profilePicture,status FROM users WHERE userId = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs  = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("userId"),
                        rs.getString("password"),
                        rs.getString("pseudo"),
                        rs.getString("profilePicture"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.out.println("🆘 getUser failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void removeUser(String userId) {
//        System.out.println("Removing user " + userId);

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE userId = ?");
             PreparedStatement pstmt2 = conn.prepareStatement("DELETE FROM userschats WHERE userId = ?")) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            pstmt2.setString(1, userId);
            pstmt2.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 removeUser failed: " + e.getMessage());
        }
    }

    @Override
    public User[] listUsers() {
//        System.out.println("Listing users");
        String sql = "SELECT userId,password,pseudo,profilePicture,status FROM users";
        List<User> userList = new ArrayList<>();

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            while (rs.next()) {
                User u = new User(
                        rs.getString("userId"),
                        rs.getString("password"),
                        rs.getString("pseudo"),
                        rs.getString("profilePicture"),
                        rs.getString("status")
                );
                userList.add(u);
            }
        } catch (SQLException e) {
            System.out.println("🆘 listUsers failed: " + e.getMessage());
        }
        return userList.toArray(new User[0]);
    }

    @Override
    public String[] listUserIdsInChat(String chatId) {
        String sql = "SELECT userId FROM userschats WHERE chatId = ?";
        Set<String> userIdSet = new HashSet<>();

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatId);
            ResultSet rs  = pstmt.executeQuery();

            while (rs.next()) {
                userIdSet.add(rs.getString("userId"));
            }

        } catch (SQLException e) {
            System.out.println("🆘 listUserIdsInChat failed: " + e.getMessage());
        }
        return userIdSet.toArray(new String[0]);
    }

    @Override
    public void userJoinsChat(String userId, String chatId) {
//        System.out.println("User " + userId + " joined chat " + chatId);
        String sql = "INSERT INTO userschats(userId,chatId) VALUES(?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, chatId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("🆘 userJoinsChat failed: " + e.getMessage());
        }
    }
}
