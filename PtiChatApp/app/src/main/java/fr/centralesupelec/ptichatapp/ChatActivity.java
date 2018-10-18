package fr.centralesupelec.ptichatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import fr.centralesupelec.ptichatapp.PODS.User;
import fr.centralesupelec.ptichatapp.PODS.Message;
import fr.centralesupelec.ptichatapp.PODS.Chat;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private EditText newMessage;

    // TODO: this is mock data, get real data
    private User sender = new User("Felix", "flx", "", "", true);
    private Chat chat = new Chat("test", "TestChat");
    private Message[] myDataset = {
            new Message("1", "Coucou", null, sender.getId(), chat.getId(), true),
            new Message("2", "Ca va ?", null, sender.getId(), chat.getId(), false)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        newMessage = findViewById(R.id.newMessage);

        recyclerView = findViewById(R.id.chatView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new MessageAdapter(myDataset);
        recyclerView.setAdapter(adapter);
    }

    public void onSend(View view) {
        // get text content
        String textContent = newMessage.getText().toString();

        // add message
        int n = myDataset.length;
        Message[] newDataset = new Message[n + 1];
        System.arraycopy(myDataset, 0, newDataset, 0, n);
        newDataset[n] = new Message("3", textContent, null, "moi", chat.getId(), false);
        myDataset = newDataset;

        // tell the view that the dataset changed
        adapter = new MessageAdapter(myDataset);
        recyclerView.swapAdapter(adapter, false);
    }
}
