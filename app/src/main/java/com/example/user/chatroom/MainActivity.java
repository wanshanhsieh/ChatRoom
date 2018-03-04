package com.example.user.chatroom;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity {

    // For internet permission
    private static final int REQUEST_INTERNET = 1;
    // For intent request
    private static final int REQUEST_LOG_IN = 1;

    String url;
    EditText edit_msg;
    Button button_send;
    ListView list_msg;
    String getNickname;

    // For Firebase Database
    Firebase mRef;
    FirebaseDatabase mDatabase;
    DatabaseReference msgRef, userRef;
    FirebaseListAdapter<ChatMessage> adapter;

    FirebaseAuth auth; // For getting current user
    FirebaseUser curUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent = getIntent();
        //getNickname = intent.getStringExtra("nickname");
        //Toast.makeText(MainActivity.this,getNickname,Toast.LENGTH_LONG).show();

        edit_msg = findViewById(R.id.edit_msg);
        button_send = findViewById(R.id.button_send);

        /* set Firebase */
        Firebase.setAndroidContext(MainActivity.this);
        /* set Firebase url */
        url = "https://chatroom-e531d.firebaseio.com/";
        mRef = new Firebase(url);
        /* Get the root of Firebase Database */
        mDatabase = FirebaseDatabase.getInstance();
        msgRef = mDatabase.getReference("msgData");
        userRef = mDatabase.getReference("userData");
        /* Get Firebase Auth */
        auth = FirebaseAuth.getInstance();
        curUser = auth.getCurrentUser();

        displayChatMsg();

        /* Press send, store the message to database */
        button_send.setOnClickListener(new Button.OnClickListener(){
            String getMsg;
            @Override
            public void onClick(View v) {
                getMsg = edit_msg.getText().toString();
                msgRef.push().setValue(new ChatMessage(
                        getMsg,
                        curUser.getEmail().toString()));
                edit_msg.setText(""); // reset
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void displayChatMsg(){

        list_msg = findViewById(R.id.list_of_messages);
        Query query = msgRef;

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.msg_layout)
                .build();

        adapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView text_user = v.findViewById(R.id.msgUser);
                TextView text_msg = v.findViewById(R.id.msgText);
                TextView text_time = v.findViewById(R.id.msgTime);

                /* Set user and msg */
                text_msg.setText(model.getmsgText());
                text_user.setText(model.getmsgUser());

                /* Format the date before showing it */
                text_time.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getmsgTime()));
            }
        };

        list_msg.setAdapter(adapter);

    }
}
