package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.Chat;
import fr.centralesupelec.ptichatapp.PODS.User;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mUsersRecyclerView;
    private RecyclerView.Adapter mUsersAdapter;
    private RecyclerView.LayoutManager mUsersLayoutManager;

    private RecyclerView mChatsRecyclerView;
    private RecyclerView.Adapter mChatsAdapter;
    private RecyclerView.LayoutManager mChatsLayoutManager;

    private TextView userNameTV;
    private TextView userStatusTV;
    private TextView userIsOnlineTV;

    private User currentUser;

    private final NewMessageReceiver newMessageReceiver = new MainActivity.NewMessageReceiver();

    private List<User> myUserDataset = new ArrayList<>();
    private List<Chat> myChatDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();

        // Register UI elements not to search them each time
        mUsersRecyclerView = findViewById(R.id.mainContactView);
        mChatsRecyclerView = findViewById(R.id.mainChatView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mUsersRecyclerView.setHasFixedSize(true);
        mChatsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mUsersLayoutManager = new LinearLayoutManager(this);
        mUsersRecyclerView.setLayoutManager(mUsersLayoutManager);

        mChatsLayoutManager = new LinearLayoutManager(this);
        mChatsRecyclerView.setLayoutManager(mChatsLayoutManager);

        // specify an mUsersAdapter
        mUsersAdapter = new ContactAdapter(myUserDataset, this);
        mUsersRecyclerView.setAdapter(mUsersAdapter);

        // specify an mChatsAdapter
        mChatsAdapter = new ChatAdapter(myChatDataset, this);
        mChatsRecyclerView.setAdapter(mChatsAdapter);

        // get the textViews
        userNameTV = findViewById(R.id.mainName);
        userStatusTV = findViewById(R.id.mainStatus);
        userIsOnlineTV = findViewById(R.id.mainIsOnline);

        // update user
        currentUser = Session.getUser();
        Log.i("MAu", "😺 Current user: " + Session.getUser());
        updateUser();

        myUserDataset.add(new User("flx", "Felix", "pic", "Give me food", true));
        myUserDataset.add(new User("rwl", "Raoul", "pic", "I like fish", false));
        mUsersAdapter.notifyDataSetChanged();

        myChatDataset.add(new Chat("id000", "First Chat"));
        myChatDataset.add(new Chat("id001", "Second Chat"));
        mChatsAdapter.notifyDataSetChanged();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onResume() {
        super.onResume();
        currentUser = Session.getUser();
        registerNewBroadcastReceiver();
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfUsers());
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChats(Session.getUserId()));
    }

    private void updateUser() {
        updateName(currentUser.getPseudo());
        updateStatus(currentUser.getStatus());
        updateOnline(currentUser.isConnected());
    }

    private void updateName(String pseudo) {
        if (!pseudo.isEmpty()) {
            userNameTV.setText(pseudo);
        }
    }

    private void updateStatus(String status) {
        if (!status.isEmpty()) {
            userStatusTV.setText(status);
        }
    }

    private void updateOnline(boolean isConnected) {
        userIsOnlineTV.setText(isConnected ? "(En ligne)" : "(Hors ligne)");
    }

    public void onLogout(View view) {
        // Switch activity to Login
        Log.i("MAc", "👈 Clicked on Logout button");
        if (Session.getUserId() != null) SendMessageTask.sendMessageAsync(this, JsonUtils.announceConnection(Session.getUserId(), false));
        Session.setUser(null);
        Utils.writeCredentials(this, null, null);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void onNewChatPressed(View view) {
        // Switch activity to Login
        Log.i("MAc", "👈 Clicked on New Chat button");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final EditText chatNameEditText = new EditText(MainActivity.this);
        chatNameEditText.setHint("Chat name...");
        chatNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        chatNameEditText.setMaxLines(1);
        chatNameEditText.requestFocus();
        chatNameEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);

        final User[] users = myUserDataset.toArray(new User[0]);
        List<String> userInfoList = new ArrayList<>();
        for (User u : users) {
            userInfoList.add(u.getId() + " - " + u.getPseudo());
        }
        final String[] userInfo = userInfoList.toArray(new String[0]);
        final boolean[] usersChecked = new boolean[userInfo.length];
        final Context maContext = this;

        alertDialogBuilder.setIcon(R.drawable.ic_chat_24dp);
        alertDialogBuilder.setTitle("Select contacts to add");
        alertDialogBuilder.setView(chatNameEditText);
        alertDialogBuilder.setMultiChoiceItems(userInfo, usersChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) { }
        });
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> selectedUserIds = new ArrayList<>();
                selectedUserIds.add(currentUser.getId());
                boolean atLeastOne = false;
                for (int i = 0; i < usersChecked.length; i++) {
                    if (usersChecked[i]) {
                        selectedUserIds.add(users[i].getId());
                        atLeastOne = true;
                    }
                }
                if (!atLeastOne) {
                    Toast.makeText(getApplicationContext(), "You need to add at least one person", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject toSend = JsonUtils.createNewChat(chatNameEditText.getText().toString(), selectedUserIds.toArray(new String[0]));
                SendMessageTask.sendMessageAsync(maContext, toSend);
            }
        });

        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public void onSelectContact(String contactId) {
        // Switch activity to Chat, using the contact name
        Log.i("MAc", "👈 Selected contact " + contactId);

        String myUserId = Session.getUserId();
        if (myUserId == null) {
            Log.w("MAc", "🆘 myUserId is null");
            return;
        }

        Intent selectChatIntent = new Intent(this, ChatActivity.class);
        selectChatIntent.putExtra("isPrivateChat", true);
        selectChatIntent.putExtra("myUserId", myUserId);
        selectChatIntent.putExtra("otherUserId", contactId);
        startActivity(selectChatIntent);
    }

    public void onSelectChat(String chatId) {
        // Switch activity to Chat, using the chat id
        Log.i("MAc", "👈 Selected chat " + chatId);
        Intent selectChatIntent = new Intent(this, ChatActivity.class);
        selectChatIntent.putExtra("isPrivateChat", false);
        selectChatIntent.putExtra("chatId", chatId);
        startActivity(selectChatIntent);
    }

    /** The activity will listen for BROADCAST_NEW_MESSAGE messages from other classes */
    private void registerNewBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_NEW_MESSAGE);
        registerReceiver(newMessageReceiver, intentFilter);
    }

    /** Receive messages from the socket interface. If login is accepted, go to main activity */
    public class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            try {
                JSONObject json = new JSONObject(message);

                if ("justText".equals(json.getString("type"))) {
                    Log.i("MAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("listOfUsers".equals(json.getString("type"))) {
//                    Log.i("MAu", "🗒 Got list of users message");
                    myUserDataset.clear();
                    for (User u : JsonUtils.listOfUsersJsonToUsers(json)) {
                        if (!u.getId().equals(Session.getUserId())) {
                            myUserDataset.add(u);
                        }
                    }
                    mUsersAdapter.notifyDataSetChanged();
                    currentUser.setConnected(true);

                } else if ("listOfChats".equals(json.getString("type"))) {
//                    Log.i("MAc", "🗒 Got list of chats message");
                    myChatDataset.clear();
                    myChatDataset.addAll(Arrays.asList(JsonUtils.listOfChatsJsonToUsers(json)));
                    mChatsAdapter.notifyDataSetChanged();
                    currentUser.setConnected(true);

                } else if ("announceConnection".equals(json.getString("type"))) {
                    boolean connection = json.getBoolean("connection");
                    String userId = json.getString("userId");
                    for (int i = 0; i < myUserDataset.size(); i++) {
                        User u = myUserDataset.get(i);
                        if (u.getId().equals(userId)) {
                            if (u.isConnected() != connection) {
                                u.setConnected(connection);
                                mUsersAdapter.notifyItemChanged(i);
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                Log.e("MAe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
