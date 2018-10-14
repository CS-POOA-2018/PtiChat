package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;

public class MainActivity extends AppCompatActivity {

    private final NewMessageReceiver newMessageReceiver = new NewMessageReceiver();

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creates and run the Socket Client Connector
        SendMessageTask.sendMessageAsync(this, "cc");

        // Register UI elements not to search them each time
        mTextView = findViewById(R.id.textView);

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
        SendMessageTask.sendMessageAsync(this, "brb");
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
        SendMessageTask.sendMessageAsync(this, "re");
    }

    public void onPlopButtonClicked(View v) {
        Log.i("MAb", "👈 Plop button clicked!");
        SendMessageTask.sendMessageAsync(this, "PLP");
    }

    private void registerNewBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_NEW_MESSAGE);
        registerReceiver(newMessageReceiver, intentFilter);
    }

    public class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            mTextView.setText(message);
        }
    }
}
