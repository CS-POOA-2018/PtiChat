package fr.centralesupelec.ptichatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText nameField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get fields
        nameField = findViewById(R.id.pseudoLogin);
        passwordField = findViewById(R.id.passwordLogin);
    }

    public void onConnect(View view) {
        // get login and password
        String login = nameField.getText().toString();
        String password = passwordField.getText().toString();

        // connect
        Session.connect(login, password);

        // switch activity to main
        // Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }
}
