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
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.User;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView userName;
    private TextView userStatus;
    private TextView userIsOnline;

    private final NewMessageReceiver newMessageReceiver = new MainActivity.NewMessageReceiver();

    private TextView mSocketTempTextView;  // TEMP SOCKET

    // TODO: this is mock data, get real data
    private User[] myDataset = {
        new User("flx", "Felix", "pic", "Give me food", true),
        new User("rwl", "Raoul", "pic", "I like fish", false)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register UI elements not to search them each time  // TEMP SOCKET
        mSocketTempTextView = findViewById(R.id.socketTempTextView2);  // TEMP SOCKET

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();

        recyclerView = findViewById(R.id.mainContactView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new ContactAdapter(myDataset);
        recyclerView.setAdapter(adapter);

        // get the textViews
        userName = findViewById(R.id.mainName);
        userStatus = findViewById(R.id.mainStatus);
        userIsOnline = findViewById(R.id.mainIsOnline);

        // update user
        updateUser();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
        SendMessageTask.sendMessageAsync(this, "brb");  // TEMP ?
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
        SendMessageTask.sendMessageAsync(this, "re");  // TEMP ?
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfUsers());
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChats(Session.getUser().getId()));
    }

    private void updateUser() {
        User currentUser = Session.getUser();
        updateName(currentUser.getPseudo());
        updateStatus(currentUser.getStatus());
        updateOnline(currentUser.isConnected());
    }

    private void updateName(String pseudo) {
        if (!pseudo.isEmpty()) {
            userName.setText(pseudo);
        }
    }

    private void updateStatus(String status) {
        if (!status.isEmpty()) {
            userStatus.setText(status);
        }
    }

    private void updateOnline(Boolean isConnected) {
        userIsOnline.setText(isConnected ? "(En ligne)" : "(Hors ligne)");
    }

    public void onSelectChat(View view) {
        // switch activy to Chat
        // TODO : switch to the right chat and not just on the activity
        Intent selectChatIntent = new Intent(this, ChatActivity.class);
        startActivity(selectChatIntent);
    }

    /** TEMP SOCKET */
    public void onPlopButtonClicked2(View v) {
        Log.i("MAb", "👈 Plop button clicked!");
        SendMessageTask.sendMessageAsync(this, "PLP");
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
            mSocketTempTextView.setText(message);

            try {
                JSONObject json = new JSONObject(message);

                if ("listOfUsers".equals(json.getString("type"))) {
                    Log.i("LAu", "🗒 Got list of users message");
                    // TODO

                } else if ("listOfChats".equals(json.getString("type"))) {
                    Log.i("LAc", "🗒 Got list of chats message");
                    // TODO

                }
            } catch (JSONException e) {
                System.out.println("Could not parse message as JSON");
            }
        }
    }
}
