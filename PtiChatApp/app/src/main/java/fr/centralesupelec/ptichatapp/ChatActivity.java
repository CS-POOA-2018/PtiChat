package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import fr.centralesupelec.ptichatapp.PODS.User;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView.Adapter mMemberAdapter;

    private RecyclerView mMessagesRecyclerView;
    private RecyclerView.Adapter mMessagesAdapter;

    private boolean mIsPrivateChat;
    private String mChatId;
    private String mMyUserId;
    private String mOtherUserId;
    private EditText newMessage;

    private final NewMessageReceiver newMessageReceiver = new ChatActivity.NewMessageReceiver();

    private List<Message> messageDataset = new ArrayList<>();
    private Map<String, Integer> mPendingMessages = new HashMap<>();

    private List<User> memberDataset = new ArrayList<>();

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

        // Add the toolbar
        Toolbar myToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);

        // Add the return arrow
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        applyChatInfoFromIntent();

        // set the right image for the chan depending on the private/public parameter
        ImageView chanImage = findViewById(R.id.chatAvatar);
        if (!mIsPrivateChat) {
            chanImage.setImageResource(R.drawable.cat_set);
        }

        // request list of members
        if (mIsPrivateChat) {
            // TODO : get pseudo and not ID of other user
            User otherUser = new User(mOtherUserId, mOtherUserId, null, null, true);
            memberDataset.add(otherUser);
        } else {
            SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChatMembers(mChatId));
        }

        // set recyclerView for members
        RecyclerView memberRecyclerView = findViewById(R.id.listOfMembers);
        memberRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager memberLayoutManager = new LinearLayoutManager(this);
        memberRecyclerView.setLayoutManager(memberLayoutManager);

        mMemberAdapter = new MemberAdapter(memberDataset);
        memberRecyclerView.setAdapter(mMemberAdapter);

        // set recyclerView for messages
        newMessage = findViewById(R.id.newMessage);
        mMessagesRecyclerView = findViewById(R.id.chatView);
        mMessagesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager messagesLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(messagesLayoutManager);

        mMessagesAdapter = new MessageAdapter(messageDataset);
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);

        // Set up listener for layout size change (keyboard appears)
        mMessagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMessagesRecyclerView.scrollToPosition(messageDataset.size() - 1);
            }
        });

        // Set up listener for enter key
        setupEnterListener(newMessage);
    }

    /** Menu icons are inflated just as they were with actionbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    /** One of the menu icons has been clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete_chat:
                Log.i("CAa", "Chat deletion asked for chatId " + mChatId);
                JSONObject toSend = JsonUtils.deleteChatJson(mChatId);
                if (toSend != null) SendMessageTask.sendMessageAsync(this, toSend);
                finish();
                return true;

            default:
                // Not recognized, invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
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
        messageDataset.add(newMessage);

        int positionInserted = messageDataset.size() - 1;
        mMessagesAdapter.notifyItemInserted(positionInserted);
        mMessagesRecyclerView.scrollToPosition(positionInserted);

        // Send the message to the back
        mPendingMessages.put(newMessage.getId(), positionInserted);
        SendMessageTask.sendMessageAsync(this, JsonUtils.sendNewMessageJson(newMessage));
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
                    Log.i("CAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("listOfChatMembers".equals(json.getString("type"))) {
                    Log.i("CAl", "🗒 Got list of members in chat");
                    memberDataset.clear();
                    Collections.addAll(memberDataset, JsonUtils.listOfUsersJsonToUsers(json));
                    mMemberAdapter.notifyDataSetChanged();

                } else if ("listMessagesChat".equals(json.getString("type"))) {
                    Log.i("CAl", "🗒 Got list of messages in chat");
                    if (mChatId == null) mChatId = json.getString("chatId");
                    messageDataset.clear();
                    Collections.addAll(messageDataset, JsonUtils.listOfMessagesJsonToMessages(json));
                    mMessagesAdapter.notifyDataSetChanged();
                    mMessagesRecyclerView.scrollToPosition(messageDataset.size() - 1);

                } else if ("newMessageInChat".equals(json.getString("type"))) {
                    if (json.getString("chatId").equals(mChatId)) {
                        Log.i("CAn", "🗒 Got new message in current chat");
                        Message newMessage = JsonUtils.messageJsonToMessage(json.getJSONObject("message"));
                        if (newMessage == null) throw new JSONException("newMessage is null");

                        String newMessageId = newMessage.getId();
                        Integer pendingMessagePosition = mPendingMessages.get(newMessageId);

                        if (pendingMessagePosition != null) {
                            messageDataset.set(pendingMessagePosition, newMessage);
                            mMessagesAdapter.notifyItemChanged(pendingMessagePosition);
                            mPendingMessages.remove(newMessageId);
                        } else {
                            messageDataset.add(newMessage);
                            mMessagesAdapter.notifyItemInserted(messageDataset.size() - 1);
                        }
                        mMessagesRecyclerView.scrollToPosition(messageDataset.size() - 1);
                    } else {
                        Log.i("CAn", "🗒 Got new message in another chat");
                    }
                }
            } catch (JSONException e) {
                Log.e("CAe", "🆘 Could not parse message as JSON");
            }
        }
    }

    /**
     * The message box will listen for the Enter key, and send the message if the user uses it
     */
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
