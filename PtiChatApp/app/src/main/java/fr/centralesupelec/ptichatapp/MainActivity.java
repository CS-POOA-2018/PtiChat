package fr.centralesupelec.ptichatapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.centralesupelec.ptichatapp.NativeSocketClient.SendMessageTask;
import fr.centralesupelec.ptichatapp.PODS.Chat;
import fr.centralesupelec.ptichatapp.PODS.User;
import fr.centralesupelec.ptichatapp.PODS.UserOnlineComparator;


public class MainActivity extends AppCompatActivity {

    private RecyclerView.Adapter mUsersAdapter;
    private RecyclerView.Adapter mChatsAdapter;

    private View mMainView;
    private TextView userNameTV;
    private TextView userStatusTV;
    private TextView userIsOnlineTV;

    private User mCurrentUser;

    private final NewMessageReceiver newMessageReceiver = new MainActivity.NewMessageReceiver();

    private List<User> mUserDataset = new ArrayList<>();
    private List<Chat> mChatDataset = new ArrayList<>();

    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();

        // Register UI elements not to search them each time
        RecyclerView usersRecyclerView = findViewById(R.id.mainContactView);
        RecyclerView chatsRecyclerView = findViewById(R.id.mainChatView);
        mMainView = findViewById(R.id.main);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        usersRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager usersLayoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(usersLayoutManager);

        RecyclerView.LayoutManager chatsLayoutManager = new LinearLayoutManager(this);
        chatsRecyclerView.setLayoutManager(chatsLayoutManager);

        // specify an mUsersAdapter
        mUserDataset.sort(new UserOnlineComparator());
        mUsersAdapter = new ContactAdapter(mUserDataset, this);
        usersRecyclerView.setAdapter(mUsersAdapter);

        // specify an mChatsAdapter
        mChatsAdapter = new ChatAdapter(mChatDataset, this);
        chatsRecyclerView.setAdapter(mChatsAdapter);

        // get the textViews
        userNameTV = findViewById(R.id.mainName);
        userStatusTV = findViewById(R.id.mainStatus);
        userIsOnlineTV = findViewById(R.id.mainIsOnline);

        // update user
        mCurrentUser = Session.getUser();
        Log.i("MAu", "😺 Current user: " + Session.getUser());
        updateUser();

        mUserDataset.add(new User("not_a_user", "Loading users...", "", "", false));
        mUsersAdapter.notifyDataSetChanged();

        mChatDataset.add(new Chat("not_a_chat", "Loading chats...", false));
        mChatsAdapter.notifyDataSetChanged();

        // set up the listeners
        setupEnterListener(this);

        mServiceIntent = new Intent(this, BackgroundListener.class);
        if (!isBackgroundListenerRunning()) startService(mServiceIntent);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("MAd", "☁️ MainActivity destroyed");
        stopService(mServiceIntent);
        try {
            unregisterReceiver(newMessageReceiver);
        } catch (IllegalArgumentException ignored) { }
    }

    public void onResume() {
        super.onResume();
        mCurrentUser = Session.getUser();
        registerNewBroadcastReceiver();
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfUsers());
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChats(Session.getUserId()));
    }

    // ----- ACTION BAR ----- //

    /** Menu icons are inflated just as they were with actionbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /** One of the menu icons has been clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete_user:
                Log.i("CAa", "💥 User deletion asked for user " + mCurrentUser.getId());
                JSONObject toSend = JsonUtils.deleteUserJson(mCurrentUser.getId());
                if (toSend != null) SendMessageTask.sendMessageAsync(this, toSend);
                onLogout(null);
                return true;

            default:
                // Not recognized, invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // ----- UI EDITION ----- //

    private void updateUser() {
        updateName(mCurrentUser.getPseudo());
        updateStatus(mCurrentUser.getStatus());
        updateOnline(mCurrentUser.isConnected());
    }

    private void updateName(String pseudo) {
        if (!pseudo.isEmpty()) {
            userNameTV.setText(pseudo);
        }
    }

    private void updateStatus(String status) {
        if (!status.isEmpty()) {
            userStatusTV.setText(status);
        }
    }

    private void updateOnline(boolean isConnected) {
        userIsOnlineTV.setText(isConnected ? "(En ligne)" : "(Hors ligne)");
    }

    private void updateUserInfo() {
        // update the user object
        String newPseudo = userNameTV.getText().toString();
        String newStatus = userStatusTV.getText().toString();

        boolean somethingChanged = false;
        if (!mCurrentUser.getPseudo().equals(newPseudo)) {
            mCurrentUser.setPseudo(newPseudo);
            somethingChanged = true;
        }
        if (!mCurrentUser.getStatus().equals(newStatus)) {
            mCurrentUser.setStatus(newStatus);
            somethingChanged = true;
        }
        if (somethingChanged) {
            Log.i("MAu", "🌱 New User info: " + newPseudo + " ; " + newStatus);
            // send the new data via the API
            JSONObject toSend = JsonUtils.editUserJson(mCurrentUser);
            if (toSend != null) SendMessageTask.sendMessageAsync(this, toSend);
        }
    }

    // ----- CREATE NEW CHAT DIALOG ----- //

    /** When "new chat" button is pressed, show dialog to choose contacts and set chat name */
    public void onNewChatPressed(View view) {
        Log.i("MAc", "👈 Clicked on New Chat button");
        final Context maContext = this;

        final EditText chatNameEditText = new EditText(MainActivity.this);
        chatNameEditText.setHint("Chat name...");
        chatNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        chatNameEditText.setMaxLines(1);
        chatNameEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);

        final User[] users = mUserDataset.toArray(new User[0]);
        List<String> userInfoList = new ArrayList<>();
        for (User u : users) {
            userInfoList.add(u.getId() + " - " + u.getPseudo());
        }
        final String[] userInfo = userInfoList.toArray(new String[0]);
        final boolean[] usersChecked = new boolean[userInfo.length];

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setIcon(R.drawable.ic_chat_24dp);
        alertDialogBuilder.setTitle("Select contacts to add");
        alertDialogBuilder.setView(chatNameEditText);
        alertDialogBuilder.setMultiChoiceItems(userInfo, usersChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) { }
        });
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> selectedUserIds = new ArrayList<>();
                selectedUserIds.add(mCurrentUser.getId());
                boolean atLeastOne = false;
                for (int i = 0; i < usersChecked.length; i++) {
                    if (usersChecked[i]) {
                        selectedUserIds.add(users[i].getId());
                        atLeastOne = true;
                    }
                }
                if (!atLeastOne) {
                    Toast.makeText(getApplicationContext(), "You need to add at least one person", Toast.LENGTH_LONG).show();
                    return;
                }
                String newChatName = chatNameEditText.getText().toString().trim();
                if (newChatName.isEmpty()) return;
                JSONObject toSend = JsonUtils.createNewChat(newChatName, selectedUserIds.toArray(new String[0]));
                SendMessageTask.sendMessageAsync(maContext, toSend);
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

    // ----- NAVIGATION TO OTHER ACTIVITIES ----- //

    public void onLogout(View view) {
        // Switch activity to Login
        Log.i("MAc", "👈 Clicked on Logout button");
        if (Session.getUserId() != null) SendMessageTask.sendMessageAsync(this, JsonUtils.announceConnection(Session.getUserId(), false));
        Session.setUser(null);
        Utils.writeCredentials(this, null, null);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private String chatNameById(String chatId) {
        for (Chat c : mChatDataset) if (chatId.equals(c.getId())) return c.getName();
        return "ChatNotFound";
    }

    private String userPseudoById(String userId) {
        for (User u : mUserDataset) if (userId.equals(u.getId())) return u.getPseudo();
        return "UserNotFound";
    }

    public void onSelectContact(String contactId) {
        // Switch activity to Chat, using the contact name
        Log.i("MAc", "👈 Selected contact " + contactId);

        String myUserId = Session.getUserId();
        if (myUserId == null) {
            Log.w("MAc", "🆘 myUserId is null");
            return;
        }

        Intent selectChatIntent = new Intent(this, ChatActivity.class);
        selectChatIntent.putExtra("isPrivateChat", true);
        selectChatIntent.putExtra("chatId", Utils.twoUserIdsToPrivateChatId(myUserId, contactId));
        selectChatIntent.putExtra("chatName", userPseudoById(contactId));
        selectChatIntent.putExtra("myUserId", myUserId);
        selectChatIntent.putExtra("otherUserId", contactId);
        startActivity(selectChatIntent);
    }

    public void onSelectChat(String chatId) {
        // Switch activity to Chat, using the chat id
        Log.i("MAc", "👈 Selected chat " + chatId);
        Intent selectChatIntent = new Intent(this, ChatActivity.class);
        selectChatIntent.putExtra("isPrivateChat", false);
        selectChatIntent.putExtra("chatId", chatId);
        selectChatIntent.putExtra("chatName", chatNameById(chatId));
        selectChatIntent.putExtra("myUserId", Session.getUserId());
        startActivity(selectChatIntent);
    }

    // ----- BACKGROUND LISTENER MANAGEMENT ----- //

    private boolean isBackgroundListenerRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        String blClassName = BackgroundListener.class.getName();
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (blClassName.equals(service.service.getClassName())) {
                Log.i ("MAs", "👂 isMyServiceRunning? true");
                return true;
            }
        }
        Log.i ("MAs", "👂 isMyServiceRunning? false");
        return false;
    }

    // ----- RECEIVER FOR MESSAGES FROM THE SOCKET ----- //

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
                    Log.i("MAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("listOfUsers".equals(json.getString("type"))) {
//                    Log.i("MAu", "🗒 Got list of users message");
                    mUserDataset.clear();
                    for (User u : JsonUtils.listOfUsersJsonToUsers(json)) {
                        if (!u.getId().equals(Session.getUserId())) {
                            mUserDataset.add(u);
                        }
                    }
                    mUserDataset.sort(new UserOnlineComparator());
                    mUsersAdapter.notifyDataSetChanged();
                    mCurrentUser.setConnected(true);

                } else if ("listOfChats".equals(json.getString("type"))) {
//                    Log.i("MAc", "🗒 Got list of chats message");
                    mChatDataset.clear();
                    mChatDataset.addAll(Arrays.asList(JsonUtils.listOfChatsJsonToChats(json)));
                    mChatsAdapter.notifyDataSetChanged();
                    mCurrentUser.setConnected(true);

                } else if ("announceConnection".equals(json.getString("type"))) {
                    boolean connection = json.getBoolean("connection");
                    String userId = json.getString("userId");
                    for (int i = 0; i < mUserDataset.size(); i++) {
                        User u = mUserDataset.get(i);
                        if (u.getId().equals(userId)) {
                            if (u.isConnected() != connection) {
                                u.setConnected(connection);
                                mUsersAdapter.notifyItemChanged(i);
                            }
                        }
                    }

                } else if ("userEditAcceptance".equals(json.getString("type"))) {
                    boolean editSuccess = json.getBoolean("value");
                    if (editSuccess) {
                        Toast.makeText(getApplicationContext(), "Successfully edited profile !", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Couldn't edit profile...", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                Log.e("MAe", "🆘 Could not parse message as JSON");
            }
        }
    }

    // ----- ACTIONS WITH THE KEYBOARD ----- //

    /** The Pseudo and Status input will listen for the Enter key, and try to update the user data */
    public void setupEnterListener(final Activity activity) {
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean pressedEnter = actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE;
                if (event != null) {
                    pressedEnter = pressedEnter || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN);
                }
                if (pressedEnter) {
                    mMainView.requestFocus();  // This will also trigger the focus change below
                    return true;
                }
                return false;
            }
        };
        TextView.OnFocusChangeListener focusChangeListener = new TextView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!userNameTV.hasFocus() && !userStatusTV.hasFocus()) {
                    // Both profile edition texts have lost focus, this count as validation
                    mMainView.requestFocus();
                    hideSoftKeyboard(activity);
                    updateUserInfo();
                }
            }
        };

        userNameTV.setOnEditorActionListener(enterListener);
        userNameTV.setOnFocusChangeListener(focusChangeListener);

        userStatusTV.setOnEditorActionListener(enterListener);
        userStatusTV.setOnFocusChangeListener(focusChangeListener);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;

        View currentFocus = activity.getCurrentFocus();
        if (currentFocus == null) return;

        inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
}
