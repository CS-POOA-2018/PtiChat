package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.User;

public class LoginActivity extends AppCompatActivity {

    private EditText nameField;
    private EditText passwordField;

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();

    private TextView mSocketTempTextView;  // TEMP SOCKET


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get fields
        nameField = findViewById(R.id.pseudoLogin);
        passwordField = findViewById(R.id.passwordLogin);

        // Creates and run the Socket Client Connector  // TEMP ?
        SendMessageTask.sendMessageAsync(this, "cc");  // TEMP ?

        // Register UI elements not to search them each time  // TEMP SOCKET
        mSocketTempTextView = findViewById(R.id.socketTempTextView);  // TEMP SOCKET

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();
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
    }


    public void onConnect(View view) {
        // Get login and password
        String login = nameField.getText().toString();
        String password = passwordField.getText().toString();

        // Send User connection to Backend
        JSONObject toSend = JsonUtils.userInfoToNewUserJson(login, password);
        if (toSend != null) SendMessageTask.sendMessageAsync(this, toSend);

//        // Connect
//        Session.connect(login, password);
    }

    /** TEMP SOCKET */
    public void onPlopButtonClicked(View v) {
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

                if ("loginAcceptance".equals(json.getString("type"))) {
                    Log.i("LAl", "🗒 Got login acceptance message");
                    if (json.getBoolean("value")) {
                        // Register the user info into the Session
                        User user = JsonUtils.loginAcceptanceJsonToUser(new JSONObject(json.getString("user")));
                        Session.setUser(user);
                        Log.i("LAs", "✅ Login sucessful!");

                        // Switch activity to main
                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        startActivity(mainActivityIntent);
                    } else {
                        // Login failed
                        mSocketTempTextView.setText(json.getString("message"));
                    }
                }
            } catch (JSONException e) {
                Log.e("LAe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
