package fr.centralesupelec.ptichatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import fr.centralesupelec.ptichatapp.PODS.Message;

public class BackgroundListener extends Service {

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver(this);

    @Override
    public void onCreate() {
        registerNewBroadcastReceiver();
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("BLs", "😡 BackgroundListener stopped!");
        try {
            unregisterReceiver(newMessageReceiver);
        } catch (IllegalArgumentException ignored) { }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Creates a Background Listener */
    public BackgroundListener() {
        super();
    }

    /** Allows to make the phone vibrate when a wizz is received */
    private void wizz() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // deprecated in API 26
            v.vibrate(500);
        }
    }

    /** The activity will listen for BROADCAST_NEW_MESSAGE messages from other classes */
    private void registerNewBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_NEW_MESSAGE);
        registerReceiver(newMessageReceiver, intentFilter);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager == null) return;

            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("messages", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

    /** Receive messages from the socket controller */
    public class NewMessageReceiver extends BroadcastReceiver {
        public BackgroundListener backgroundListener;

        public NewMessageReceiver(BackgroundListener backgroundListener) {
            this.backgroundListener = backgroundListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            try {
                JSONObject json = new JSONObject(message);
                if ("newMessageInChat".equals(json.getString("type"))) {
                    Message newMessage = JsonUtils.messageJsonToMessage(json.getJSONObject("message"));
                    if (newMessage != null) {
                        String newMessageContent = newMessage.getContent();
                        if (!newMessage.getSenderId().equals(Session.getUserId())) {
                            Intent redirect = new Intent(backgroundListener, ChatActivity.class);
                            redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            // Check if chat is private or not and gives info about it to the incoming activity
                            redirect.putExtra("isPrivateChat", !newMessage.getChatId().contains("+"));
                            if (newMessage.getChatId().contains("+")){
                                redirect.putExtra("chatId", newMessage.getChatId());
                            } else {
                                redirect.putExtra("myUserId", Session.getUserId());
                                redirect.putExtra("otherUserId", newMessage.getSenderId());
                            }
                            PendingIntent pendingIntent = PendingIntent.getActivity(backgroundListener, UUID.randomUUID().hashCode(), redirect, PendingIntent.FLAG_UPDATE_CURRENT);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(backgroundListener, "messages")
                                    .setSmallIcon(R.drawable.ic_chat_24dp)
                                    .setContentTitle("New message!")
                                    .setContentText(newMessageContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(backgroundListener);

                            // NotificationId is a int for each notification. If it is the same it replaces notif...
                            notificationManager.notify(newMessage.getChatId().hashCode(), mBuilder.build());
                        }
                        if (newMessageContent.equals(":wizz:")) {
                            wizz();
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("BLe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
