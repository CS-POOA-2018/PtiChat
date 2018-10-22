package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
        mUsersAdapter = new ContactAdapter(myUserDataset);
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
//        SendMessageTask.sendMessageAsync(this, "brb");  // TEMP ?
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
//        SendMessageTask.sendMessageAsync(this, "re");  // TEMP ?
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfUsers());
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChats(Session.getUser().getId()));
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

//    public void onSelectChat(View view) {
//        // switch activity to Chat
//        // TO.DO : switch to the right chat and not just on the activity
//        Intent selectChatIntent = new Intent(this, ChatActivity.class);
//        startActivity(selectChatIntent);
//    }

    public void onSelectChat(String chatId) {
        // Switch activity to Chat
        Log.i("MAc", "👈 Selected chat " + chatId);
        Intent selectChatIntent = new Intent(this, ChatActivity.class);
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
                        if (!u.getId().equals(Session.getUser().getId())) {
                            myUserDataset.add(u);
                        }
                    }
                    mUsersAdapter.notifyDataSetChanged();

                } else if ("listOfChats".equals(json.getString("type"))) {
//                    Log.i("MAc", "🗒 Got list of chats message");
                    myChatDataset.clear();
                    myChatDataset.addAll(Arrays.asList(JsonUtils.listOfChatsJsonToUsers(json)));
                    mChatsAdapter.notifyDataSetChanged();

                }
            } catch (JSONException e) {
                Log.e("MAe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
