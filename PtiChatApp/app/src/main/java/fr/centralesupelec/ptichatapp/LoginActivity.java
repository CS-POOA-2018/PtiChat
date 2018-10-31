package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.NativeSocketClient.SocketSingleton;
import fr.centralesupelec.ptichatapp.PODS.User;


public class LoginActivity extends AppCompatActivity {

    private EditText nameField;
    private EditText passwordField;
    private EditText hostNameField;
    private EditText hostPortField;

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();

    // TODO : this is a debug option plz remove for prod
    private boolean backIsWorking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar myToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(myToolbar);

        // Get fields
        nameField = findViewById(R.id.pseudoLogin);
        passwordField = findViewById(R.id.passwordLogin);
        hostNameField = findViewById(R.id.hostNameEditText);
        hostPortField = findViewById(R.id.hostPortEditText);

        // Fill in login EditText objects
        Pair<String, String> credentials = Utils.getCredentials(this);
        if (credentials.first != null && credentials.second != null) {
            nameField.setText(credentials.first);
            passwordField.setText(credentials.second);
        }

        // Fill in host info EditText objects
        Pair<String, Integer> hostInfo = Utils.getHostInfo(this);
        hostNameField.setText(hostInfo.first);
        hostPortField.setText(String.valueOf(hostInfo.second));

        if (backIsWorking) {
            // Creates and run the Socket Client Connector, this will make the login faster  // TEMP ?
            SendMessageTask.sendMessageAsync(this, JsonUtils.justTextJSON("cc"));  // TEMP ?

            // Register the receiver for new incoming message
            registerNewBroadcastReceiver();
        }

        // Input password listens for the Enter key
        setupEnterListener();
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

        // Write them into phone storage
        Utils.writeCredentials(this, login, password);

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
            finish();
        }
    }

    public void onHostNameChanged(View view) {
        // Get host name and port
        String hostName = hostNameField.getText().toString();
        int hostPort = Integer.valueOf(hostPortField.getText().toString());
        if (hostName.isEmpty() || hostPort == 0) {
            return;
        }
        // Save to shared preferences
        Utils.writeHostInfo(this, hostName, hostPort);
        // Reconnect
        SocketSingleton.renewSocketClient();
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
                    Log.i("LAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("loginAcceptance".equals(json.getString("type"))) {
                    Log.i("LAl", "🗒 Got login acceptance message");
                    if (json.getBoolean("value")) {
                        // Register the user info into the Session
                        User user = JsonUtils.loginAcceptanceJsonToUser(json);
                        Session.setUser(user);
                        Log.i("LAs", "✅ Login successful! User: " + ((user != null) ? user : "null"));
                        // Switch activity to main
                        Intent mainActivityIntent = new Intent(context, MainActivity.class);
                        startActivity(mainActivityIntent);
                        finish();
                    } else {
                        // Login failed
                        Log.i("LAr", "❌ Login failed: " + json.getString("message"));
                        Toast.makeText(getApplicationContext(), "Error: " + json.getString("message"), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                Log.e("LAe", "🆘 Could not parse message as JSON: " + e.getMessage());
            }
        }
    }

    /** The password input will listen for the Enter key, and try to connect if the user uses it */
    public void setupEnterListener() {
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean pressedEnter = actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE;
                if (event != null) {
                    pressedEnter = pressedEnter || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN);
                }
                if (pressedEnter) {
                    onConnect(passwordField);
                    return true;
                }
                return false;
            }
        };
        passwordField.setOnEditorActionListener(enterListener);
    }
}
