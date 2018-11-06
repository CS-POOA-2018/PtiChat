package fr.centralesupelec.ptichatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
import fr.centralesupelec.ptichatapp.PODS.Chat;
import fr.centralesupelec.ptichatapp.PODS.Message;
import fr.centralesupelec.ptichatapp.PODS.User;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView.Adapter mMemberAdapter;

    private RecyclerView mMessagesRecyclerView;
    private RecyclerView.Adapter mMessagesAdapter;

    private boolean mIsPrivateChat;
    private String mChatId;
    private String mChatName;
    private String mMyUserId;
    private String mOtherUserId;
    private EditText newMessage;

    private final NewMessageReceiver newMessageReceiver = new ChatActivity.NewMessageReceiver();

    private List<Message> messageDataset = new ArrayList<>();
    private Map<String, Integer> mPendingMessages = new HashMap<>();

    private List<User> memberDataset = new ArrayList<>();

    private void applyChatInfoFromIntent() {
        mIsPrivateChat = getIntent().getBooleanExtra("isPrivateChat", false);
        mChatId = getIntent().getStringExtra("chatId");
        mChatName = getIntent().getStringExtra("chatName");
        mMyUserId = getIntent().getStringExtra("myUserId");
        if (mIsPrivateChat) mOtherUserId = getIntent().getStringExtra("otherUserId");
        setActionBarTitle();
    }

    private void setActionBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && mChatName != null) actionBar.setTitle(mChatName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        applyChatInfoFromIntent();

        // Add the toolbar
        Toolbar myToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);

        // Add the return arrow
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // Set the right image for the chan depending on the private/public parameter
        ImageView chanImage = findViewById(R.id.chatAvatar);
        if (!mIsPrivateChat) {
            chanImage.setImageResource(R.drawable.cat_set);
        }

        // Set recyclerView for members
        RecyclerView memberRecyclerView = findViewById(R.id.listOfMembers);
        memberRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager memberLayoutManager = new LinearLayoutManager(this);
        memberRecyclerView.setLayoutManager(memberLayoutManager);

        mMemberAdapter = new MemberAdapter(memberDataset);
        memberRecyclerView.setAdapter(mMemberAdapter);

        // Set recyclerView for messages
        newMessage = findViewById(R.id.newMessage);
        mMessagesRecyclerView = findViewById(R.id.chatView);
        mMessagesRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager messagesLayoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(messagesLayoutManager);

        messageDataset.add(new Message("Loading messages...", "system", mChatId));
        mMessagesAdapter = new MessageAdapter(messageDataset, this);
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
        if (mIsPrivateChat) menu.findItem(R.id.action_rename_chat).setVisible(false);
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

            case R.id.action_rename_chat:
                Log.i("CAa", "Chat renaming asked for chatId " + mChatId);
                onRenameChat();
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

        // Request list of messages
        if (mIsPrivateChat) {
            SendMessageTask.sendMessageAsync(this, JsonUtils.askForPrivateMessages(mMyUserId, mOtherUserId));
        } else {
            SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfMessages(mChatId));
        }

        // Request list of members
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChatMembers(mChatId));
    }

    String getMyUserId() {
        return (mMyUserId == null) ? Session.getUserId() : mMyUserId;
    }

    public void onSend(View view) {
        // get text content and empty the input field
        String textContent = newMessage.getText().toString();
        if (textContent.isEmpty()) return;
        newMessage.setText("");

        // Create and display the new message immediately
        Message newMessage = new Message(textContent, getMyUserId(), mChatId);
        messageDataset.add(newMessage);

        int positionInserted = messageDataset.size() - 1;
        mMessagesAdapter.notifyItemInserted(positionInserted);
        mMessagesRecyclerView.scrollToPosition(positionInserted);

        // Send the message to the back
        mPendingMessages.put(newMessage.getId(), positionInserted);
        SendMessageTask.sendMessageAsync(this, JsonUtils.sendNewMessageJson(newMessage));
    }

    public void onRenameChat() {
        final Context caContext = this;

        final EditText chatNameEditText = new EditText(ChatActivity.this);
        chatNameEditText.setText(mChatName);
        chatNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        chatNameEditText.setMaxLines(1);
        chatNameEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatActivity.this);
        alertDialogBuilder.setIcon(R.drawable.ic_chat_24dp);
        alertDialogBuilder.setTitle("Select new chat name");
        alertDialogBuilder.setView(chatNameEditText);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newChatName = chatNameEditText.getText().toString().trim();
                if (newChatName.isEmpty() || newChatName.equals(mChatName)) return;
                JSONObject toSend = JsonUtils.editChatJson(new Chat(mChatId, newChatName, false));
                SendMessageTask.sendMessageAsync(caContext, toSend);
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        chatNameEditText.requestFocus();
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

                    // If private chat, update title with contact pseudo
                    if (mIsPrivateChat)
                        for (User u : memberDataset)
                            if (!mMyUserId.equals(u.getId()) && !mChatName.equals(u.getPseudo())) {
                                mChatName = u.getPseudo();
                                setActionBarTitle();
                            }

                } else if ("listMessagesChat".equals(json.getString("type"))) {
                    Log.i("CAl", "🗒 Got list of messages in chat");
                    messageDataset.clear();
                    Collections.addAll(messageDataset, JsonUtils.listOfMessagesJsonToMessages(json));
                    mMessagesAdapter.notifyDataSetChanged();
                    mMessagesRecyclerView.scrollToPosition(messageDataset.size() - 1);

                } else if ("newMessageInChat".equals(json.getString("type"))) {
                    if (json.getJSONObject("chat").getString("chatId").equals(mChatId)) {
                        Log.i("CAn", "🗒 Got new message in current chat");
                        Message newMessage = JsonUtils.messageJsonToMessage(json.getJSONObject("message"));
                        if (newMessage == null) throw new JSONException("newMessage is null");

                        // Check if the message is one of the pending ones (sent by current user)
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
                } else if ("chatEditAcceptance".equals(json.getString("type"))) {
                    if (json.getJSONObject("chat").getString("chatId").equals(mChatId)) {
                        boolean accepted = json.getBoolean("value");
                        Log.i("CAn", "🗒 Chat name edited: " + accepted);
                        Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                        if (accepted) {
                            Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                            mChatName = json.getJSONObject("chat").getString("chatName");
                            getIntent().putExtra("chatName", mChatName);
                            setActionBarTitle();
                        }
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
