package fr.centralesupelec.ptichatapp;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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


public class MainActivity extends AppCompatActivity {

    private RecyclerView mUsersRecyclerView;
    private RecyclerView.Adapter mUsersAdapter;
    private RecyclerView.LayoutManager mUsersLayoutManager;

    private RecyclerView mChatsRecyclerView;
    private RecyclerView.Adapter mChatsAdapter;
    private RecyclerView.LayoutManager mChatsLayoutManager;

    private TextView userNameTV;
    private TextView userStatusTV;
    private TextView userIsOnlineTV;

    private Activity mActivity;

    private User currentUser;

    private final NewMessageReceiver newMessageReceiver = new MainActivity.NewMessageReceiver();

    private List<User> myUserDataset = new ArrayList<>();
    private List<Chat> myChatDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(findViewById(R.id.main));

        // Register the receiver for new incoming message
        registerNewBroadcastReceiver();

        // Register UI elements not to search them each time
        mUsersRecyclerView = findViewById(R.id.mainContactView);
        mChatsRecyclerView = findViewById(R.id.mainChatView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mUsersRecyclerView.setHasFixedSize(true);
        mChatsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mUsersLayoutManager = new LinearLayoutManager(this);
        mUsersRecyclerView.setLayoutManager(mUsersLayoutManager);

        mChatsLayoutManager = new LinearLayoutManager(this);
        mChatsRecyclerView.setLayoutManager(mChatsLayoutManager);

        // specify an mUsersAdapter
        mUsersAdapter = new ContactAdapter(myUserDataset, this);
        mUsersRecyclerView.setAdapter(mUsersAdapter);

        // specify an mChatsAdapter
        mChatsAdapter = new ChatAdapter(myChatDataset, this);
        mChatsRecyclerView.setAdapter(mChatsAdapter);

        // get the textViews
        userNameTV = findViewById(R.id.mainName);
        userStatusTV = findViewById(R.id.mainStatus);
        userIsOnlineTV = findViewById(R.id.mainIsOnline);

        // get the activity
        mActivity = this;

        // update user
        currentUser = Session.getUser();
        Log.i("MAu", "😺 Current user: " + Session.getUser());
        updateUser();

        myUserDataset.add(new User("flx", "Felix", "pic", "Give me food", true));
        myUserDataset.add(new User("rwl", "Raoul", "pic", "I like fish", false));
        mUsersAdapter.notifyDataSetChanged();

        myChatDataset.add(new Chat("id000", "First Chat"));
        myChatDataset.add(new Chat("id001", "Second Chat"));
        mChatsAdapter.notifyDataSetChanged();

        // set up the listeners
        setupEnterListener(this);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(newMessageReceiver);
    }

    public void onResume() {
        super.onResume();
        currentUser = Session.getUser();
        registerNewBroadcastReceiver();
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfUsers());
        SendMessageTask.sendMessageAsync(this, JsonUtils.askForListOfChats(Session.getUserId()));
    }

    private void updateUser() {
        updateName(currentUser.getPseudo());
        updateStatus(currentUser.getStatus());
        updateOnline(currentUser.isConnected());
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
        startActivity(selectChatIntent);
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
                    Log.i("MAt", "🗒 Got justText message: " + json.getString("content"));
                    Toast.makeText(getApplicationContext(), json.getString("content"), Toast.LENGTH_LONG).show();

                } else if ("listOfUsers".equals(json.getString("type"))) {
//                    Log.i("MAu", "🗒 Got list of users message");
                    myUserDataset.clear();
                    for (User u : JsonUtils.listOfUsersJsonToUsers(json)) {
                        if (!u.getId().equals(Session.getUserId())) {
                            myUserDataset.add(u);
                        }
                    }
                    mUsersAdapter.notifyDataSetChanged();
                    currentUser.setConnected(true);

                } else if ("listOfChats".equals(json.getString("type"))) {
//                    Log.i("MAc", "🗒 Got list of chats message");
                    myChatDataset.clear();
                    myChatDataset.addAll(Arrays.asList(JsonUtils.listOfChatsJsonToUsers(json)));
                    mChatsAdapter.notifyDataSetChanged();
                    currentUser.setConnected(true);

                } else if ("announceConnection".equals(json.getString("type"))) {
                    boolean connection = json.getBoolean("connection");
                    String userId = json.getString("userId");
                    for (int i = 0; i < myUserDataset.size(); i++) {
                        User u = myUserDataset.get(i);
                        if (u.getId().equals(userId)) {
                            if (u.isConnected() != connection) {
                                u.setConnected(connection);
                                mUsersAdapter.notifyItemChanged(i);
                            }
                        }
                    }

                }
            } catch (JSONException e) {
                Log.e("MAe", "🆘 Could not parse message as JSON");
            }
        }
    }

    /** The Pseudo and Status input will listen for the Enter key, and try to update the user data */
    public void setupEnterListener(Activity activity) {
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean pressedEnter = actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE;
                if (event != null) {
                    pressedEnter = pressedEnter || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN);
                }
                if (pressedEnter) {
                    Log.w("PIZZA", "Hello");
                    hideSoftKeyboard(mActivity);
                    findViewById(R.id.main).requestFocus();
                    return true;
                }
                return false;
            }
        };
        userNameTV.setOnEditorActionListener(enterListener);
        userStatusTV.setOnEditorActionListener(enterListener);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /** Sets a touch listener on every view of the main activity that isn't an EditText, using recursion */
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new TextView.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    Log.w("PIZZA", "Touchy !");
                    hideSoftKeyboard(mActivity);
                    v.requestFocus();
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}
