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

        this.onConnect();
        running.start();
    }

    private class Waiter implements Runnable {

        private HelloActivity mHelloActivity;

        public Waiter(HelloActivity ha) {
            mHelloActivity = ha;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                Intent loginActivityIntent = new Intent(mHelloActivity, LoginActivity.class);
                finish();
                startActivity(loginActivityIntent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
    }

    public void onConnect() {
        // Get login and password

        Pair<String, String> credentials = Utils.getCredentials(this);
            // Send User connection to Backend
            JSONObject toSend = JsonUtils.userInfoToNewUserJson("", "");
            SendMessageTask.sendMessageAsync(this, toSend);
    }

    /**
     * The activity will listen for BROADCAST_NEW_MESSAGE messages from other classes
     */
    private void registerNewBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_NEW_MESSAGE);
        registerReceiver(newMessageReceiver, intentFilter);
    }

    /**
     * Receive messages from the socket interface. If login is accepted, go to main activity
     */
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
                        running.interrupt();
                        finish();
                        startActivity(mainActivityIntent);
                    } else {
                        // Login failed
                        Log.i("LAr", "❌ Login failed: " + json.getString("message"));
                        Intent loginActivityIntent = new Intent(context, LoginActivity.class);
                        running.interrupt();
                        finish();
                        startActivity(loginActivityIntent);
                    }
                }
            } catch (JSONException e) {
                Log.e("LAe", "🆘 Could not parse message as JSON: " + e.getMessage());
            }
        }
    }
}