package fr.centralesupelec.ptichatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView userName;
    private TextView userStatus;
    private TextView userIsOnline;

    // TODO: this is mock data, get real data
    private User[] myDataset = {
        new User("Felix", "flx", "Give me food", true),
        new User("Raoul", "rwl", "I like fish", false)
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.mainContactView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter
        adapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(adapter);

        // get the textViews
        userName = findViewById(R.id.mainName);
        userStatus = findViewById(R.id.mainStatus);
        userIsOnline = findViewById(R.id.mainIsOnline);

        // update user
        updateUser();
    }

    private void updateUser() {
        User currentUser = Session.getUser();
        updateName(currentUser.getPseudo());
        updateStatus(currentUser.getStatus());
        updateOnline(currentUser.isConnected());
    }

    private void updateName(String pseudo) {
        if (!pseudo.isEmpty()) {
            userName.setText(pseudo);
        }
    }

    private void updateStatus(String status) {
        if (!status.isEmpty()) {
            userStatus.setText(status);
        }
    }

    private void updateOnline(Boolean isConnected) {
        userIsOnline.setText(isConnected ? "(En ligne)" : "(Hors ligne)");
    }
}
