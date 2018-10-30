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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

    private boolean mIsPrivateChat;
    private String mChatId;
    private String mMyUserId;
    private String mOtherUserId;
    private EditText newMessage;

    private final NewMessageReceiver newMessageReceiver = new ChatActivity.NewMessageReceiver();

    private List<Message> myDataset = new ArrayList<>();
    private Map<String, Integer> mPendingMessages = new HashMap<>();

    private void applyChatInfoFromIntent() {
        mIsPrivateChat = getIntent().getBooleanExtra("isPrivateChat", false);
        if (mIsPrivateChat) {
            mMyUserId = getIntent().getStringExtra("myUserId");
            mOtherUserId = getIntent().getStringExtra("otherUserId");
        } else {
            mChatId = getIntent().getStringExtra("chatId");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        applyChatInfoFromIntent();

        // set the right image for the chan depending on the private/public parameter
        ImageView chanImage = findViewById(R.id.chatAvatar);
        if (!mIsPrivateChat) {
            chanImage.setImageResource(R.drawable.cat_set);
        }

        newMessage = findViewById(R.id.newMessage);
        mMessagesRecyclerView = findViewById(R.id.chatView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mMessagesRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager messagesLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(messagesLayoutManager);

        // specify an adapter
        mMessagesAdapter = new MessageAdapter(myDataset);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);

        // Set up listener for layout size change (keyboard appears)
        mMessagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom) {
                mMessagesRecyclerView.scrollToPosition(myDataset.size()-1);
            }
        });

        // Set up listener for enter key
        setupEnterListener(newMessage);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onResume() {
        super.onResume();
        registerNewBroadcastReceiver();
        applyChatInfoFromIntent();
        if (mIsPrivateChat) {
            SendMessageTask.sendMessageAsync(this, JsonUtils.askForPrivateMessages(mMyUserId, mOtherUserId));
        } else {
            SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfMessages(mChatId));
        }
    }

    public void onSend(View view) {
        // get text content and empty the input field
        String textContent = newMessage.getText().toString();
        if (textContent.isEmpty()) return;
        newMessage.setText("");

        // Create and display the new message immediately
        Message newMessage = new Message(textContent, Session.getUser().getId(), mChatId);
        myDataset.add(newMessage);

        int positionInserted = myDataset.size() - 1;
        mMessagesAdapter.notifyItemInserted(positionInserted);
        mMessagesRecyclerView.scrollToPosition(positionInserted);

        // Send the message to the back
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
                    if (mChatId == null) mChatId = json.getString("chatId");
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

    /** The message box will listen for the Enter key, and send the message if the user uses it */
    public void setupEnterListener(EditText messageBox) {
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean pressedEnter = actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE;
                if (event != null) {
                    pressedEnter = pressedEnter || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN);
                }
                if (pressedEnter) {
                    onSend(newMessage);
                    return true;
                }
                return false;
            }
        };
        messageBox.setOnEditorActionListener(enterListener);
    }
}
