package fr.centralesupelec.ptichatapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import fr.centralesupelec.ptichatapp.PODS.Message;

public class BackgroundListener extends Service {


    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();

    @Override
    public void onCreate() {
        registerNewBroadcastReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Creates a Background Listener.
     */
    public BackgroundListener() {
        super();
    }


    /***
     Allows to make the phone vibrate when a wizz is received
     ***/
    private void wizz() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
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
                Log.d("mdrrr", "mrd");
                if ("newMessageInChat".equals(json.getString("type"))) {
                    Message newMessage = JsonUtils.messageJsonToMessage(json.getJSONObject("message"));
                    if (newMessage != null) {
                        String newMessageContent = newMessage.getContent();
                        if (newMessageContent.equals(":wizz:")) {
                            wizz();
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("CAe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
