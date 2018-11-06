package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.User;


/**
 * This activity will be the first to be launched on application startup.
 * It will try to log you using the last credentials and server you were using.
 * If it succeeds, you are logged in
 * If it fails, you are redirected to the login page
 */
public class HelloActivity extends AppCompatActivity {

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();
    private Thread running = new Thread(new Waiter(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creates and run the Socket Client Connector, this will make the login faster
        SendMessageTask.sendMessageAsync(this, JsonUtils.justTextJSON("cc"));

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();

        running.start();
        onConnect();
    }

    /** Wait for a few seconds, then call onFailedLogin if not interrupted before the timeout */
    private class Waiter implements Runnable {

        private HelloActivity mHelloActivity;

        private Waiter(HelloActivity ha) {
            mHelloActivity = ha;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                onFailedLogin(mHelloActivity);
            } catch (InterruptedException e) {
                Log.i("HAi", "HelloActivity sleep interrupted");
            }
        }
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(newMessageReceiver);
        } catch (IllegalArgumentException ignored) { }
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
    }

    public void onConnect() {
        // Get login and password
        Pair<String, String> credentials = Utils.getCredentials(this);

        if (credentials.first == null || credentials.second == null || credentials.first.isEmpty() || credentials.second.isEmpty()) {
            onFailedLogin(this);
        } else {
            // Send User connection to Backend
            JSONObject toSend = JsonUtils.userInfoToNewUserJson(credentials.first, credentials.second);
            SendMessageTask.sendMessageAsync(this, toSend);
        }
    }

    private void onSuccessfulLogin(Context context) {
        // Switch activity to main
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        running.interrupt();
        startActivity(mainActivityIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void onFailedLogin(Context context) {
        // Switch activity to login page
        Intent loginActivityIntent = new Intent(context, LoginActivity.class);
        running.interrupt();
        startActivity(loginActivityIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
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
                        onSuccessfulLogin(context);
                    } else {
                        // Login failed
                        Log.i("LAr", "❌ Login failed: " + json.getString("message"));
                        onFailedLogin(context);
                    }
                }
            } catch (JSONException e) {
                Log.e("LAe", "🆘 Could not parse message as JSON: " + e.getMessage());
            }
        }
    }
}
