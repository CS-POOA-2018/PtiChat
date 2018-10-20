package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.User;

//import android.util.Log;
//import android.widget.Button;
//
//import ua.naiksoftware.stomp.Stomp;
//import ua.naiksoftware.stomp.client.StompClient;

public class LoginActivity extends AppCompatActivity {

//    private Button connect;
//    private StompClient mStompClient;

    private EditText nameField;
    private EditText passwordField;

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();

//    private TextView mSocketTempTextView;  // TEMP SOCKET
    // TODO : this is a debug option plz remove for prod
    private Boolean backIsWorking = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get fields
        nameField = findViewById(R.id.pseudoLogin);
        passwordField = findViewById(R.id.passwordLogin);

        if (backIsWorking) {
            // Creates and run the Socket Client Connector, this will make the login faster  // TEMP ?
            SendMessageTask.sendMessageAsync(this, "cc");  // TEMP ?

            // Register UI elements not to search them each time  // TEMP SOCKET
//            mSocketTempTextView = findViewById(R.id.socketTempTextView);  // TEMP SOCKET

            // Register the receiver for new incoming message
            registerNewBroadcastReceiver();

//            // Set the listener on the button
//            connect = findViewById(R.id.button2);
//            connect.setOnClickListener(v -> {
//                stompTest();
//            });
        }
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
    }

    public void onConnect(View view) {
        // Get login and password
        String login = nameField.getText().toString();
        String password = passwordField.getText().toString();

        if (backIsWorking) {
            // Send User connection to Backend
            JSONObject toSend = JsonUtils.userInfoToNewUserJson(login, password);
            if (toSend != null) SendMessageTask.sendMessageAsync(this, toSend);
        } else {
            // Connect with session on front
            Session.setUser(new User(login, login, "", "", true));
            // Switch activity to main
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
        }
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

//    /** Test the stomp connexion **/
//    public void stompTest() {
//        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://localhost:8080/chat");
//        mStompClient.connect();
//
//        mStompClient.topic("/topic/messages").subscribe(topicMessage -> {
//            System.out.println(topicMessage.getPayload());
//        });
//        mStompClient.send("/app/chat", "{\"from\":\"from\", \"text\":\"text\"}").subscribe();
//        Log.e("MAISLOL", "this is running");
//    }

    /** Receive messages from the socket interface. If login is accepted, go to main activity */
    public class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
//            mSocketTempTextView.setText(message);

            try {
                JSONObject json = new JSONObject(message);

                if ("justText".equals(json.getString("type"))) {
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("loginAcceptance".equals(json.getString("type"))) {
                    Log.i("LAl", "🗒 Got login acceptance message");
                    if (json.getBoolean("value")) {
                        // Register the user info into the Session
                        User user = JsonUtils.loginAcceptanceJsonToUser(json);
                        Session.setUser(user);
                        Log.i("LAu", "current user: " + Session.getUser());
                        Log.i("LAs", "✅ Login successful!");
                        // Switch activity to main
                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        startActivity(mainActivityIntent);
                    } else {
                        // Login failed
                        Toast.makeText(getApplicationContext(), "Error: " + json.getString("message"), Toast.LENGTH_LONG).show();
//                        mSocketTempTextView.setText(json.getString("message"));
                    }
                }
            } catch (JSONException e) {
                Log.e("LAe", "🆘 Could not parse message as JSON: " + e.getMessage());
            }
        }
    }
}
