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
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.Message;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessagesRecyclerView;
    private RecyclerView.Adapter mMessagesAdapter;
    private RecyclerView.LayoutManager mMessagesLayoutManager;

    private String mChatId;
    private EditText newMessage;

    private final NewMessageReceiver newMessageReceiver = new ChatActivity.NewMessageReceiver();

    private List<Message> myDataset = new ArrayList<>();
    private Map<String, Integer> mPendingMessages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatId = getIntent().getStringExtra("chatId");

//        myDataset.add(new Message("1", "Coucou", null, sender.getId(), chat.getId(), true));
//        myDataset.add(new Message("2", "Ca va ?", null, sender.getId(), chat.getId(), false));

        newMessage = findViewById(R.id.newMessage);

        mMessagesRecyclerView = findViewById(R.id.chatView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mMessagesRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mMessagesLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(mMessagesLayoutManager);

        // specify an adapter
        mMessagesAdapter = new MessageAdapter(myDataset);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
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
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfMessages(mChatId));
    }

    public void onSend(View view) {
        // get text content
        String textContent = newMessage.getText().toString();
        if (textContent.isEmpty()) return;
        newMessage.setText("");

        // add message
        Message newMessage = new Message(textContent, Session.getUser().getId(), mChatId);
        myDataset.add(newMessage);
        int positionInserted = myDataset.size() - 1;
//        myDataset.add(new Message("3", textContent, null, "moi", chat.getId(), false));
        mMessagesAdapter.notifyItemInserted(positionInserted);
        mMessagesRecyclerView.scrollToPosition(positionInserted);

        mPendingMessages.put(newMessage.getId(), positionInserted);

        SendMessageTask.sendMessageAsync(this, JsonUtils.sendNewMessageJson(newMessage));
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
                    Log.i("CAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("listMessagesChat".equals(json.getString("type"))) {
                    Log.i("CAl", "🗒 Got list of messages in chat");
                    myDataset.clear();
                    Collections.addAll(myDataset, JsonUtils.listOfMessagesJsonToMessages(json));
                    mMessagesAdapter.notifyDataSetChanged();
                    mMessagesRecyclerView.scrollToPosition(myDataset.size() - 1);

                } else if ("newMessageInChat".equals(json.getString("type"))) {
                    if (json.getString("chatId").equals(mChatId)) {
                        Log.i("CAn", "🗒 Got new message in current chat");
                        Message newMessage = JsonUtils.messageJsonToMessage(json.getJSONObject("message"));
                        if (newMessage == null) throw new JSONException("newMessage is null");

                        String newMessageId = newMessage.getId();
                        Integer pendingMessagePosition = mPendingMessages.get(newMessageId);

                        if (pendingMessagePosition != null) {
                            myDataset.set(pendingMessagePosition, newMessage);
                            mMessagesAdapter.notifyItemChanged(pendingMessagePosition);
                            mPendingMessages.remove(newMessageId);
                        } else {
                            myDataset.add(newMessage);
                            mMessagesAdapter.notifyItemInserted(myDataset.size() - 1);
                        }
                        mMessagesRecyclerView.scrollToPosition(myDataset.size() - 1);

                    } else {
                        Log.i("CAn", "🗒 Got new message in another chat");
                    }
                }
            } catch (JSONException e) {
                Log.e("CAe", "🆘 Could not parse message as JSON");
            }
        }
    }
}
