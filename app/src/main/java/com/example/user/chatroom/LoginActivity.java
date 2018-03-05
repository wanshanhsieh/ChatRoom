package com.example.user.chatroom;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Random;

import static android.Manifest.permission.INTERNET;

public class LoginActivity extends AppCompatActivity {

    // For internet permission
    private static final int REQUEST_INTERNET = 1;
    // For intent request
    private static final int REQUEST_LOG_IN = 1;

    // For layout
    TextView text_title, text_email, text_nickname, text_password;
    EditText edit_email, edit_nickname, edit_password;
    Button button_login, button_register;
    CheckBox box_showPassword;

    // For FirebaseAuth
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    Query queryUserRef;

    // For Firebase Database
    Firebase mRef;
    FirebaseDatabase mDatabase;
    DatabaseReference userRef;
    String url;

    String tmpNickname, tmpEmail, tmpColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login_ShowLayout();

        /**
         * Check Internet permission
         */
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    INTERNET)) {
                // Should we show an explanation?
            }
            else {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{INTERNET},
                        REQUEST_INTERNET);
            }
        }
        else {
            //Toast.makeText(LoginActivity.this, "已取得連線", Toast.LENGTH_SHORT).show();
        }

        /**
         * Check if the device has connected to internet
         */
        if (InternetStatus.getInstance(getApplicationContext()).isOnline()) {
            //Toast.makeText(getApplicationContext(), "WiFi/Mobile Networks Connected!", Toast.LENGTH_SHORT).show();
        } else { // Internet is NOT available
            Toast.makeText(LoginActivity.this, "Ooops! No WiFi/Mobile Networks Connected!", Toast.LENGTH_LONG).show();
            finish();
        }

        /**
         * Check if this user is already logged in
         */
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    // user is already logged in
                    Toast.makeText(LoginActivity.this,"Welcome!",Toast.LENGTH_LONG).show();
                    // Go to MainActivity, display chat message
                    gotoDisplayChatMsg();
                }
                else{
                    // Request user log in
                    Toast.makeText(LoginActivity.this,"Please log in first",Toast.LENGTH_LONG).show();
                }
            }
        };

        /**
         * button login: call login function
         */
        button_login.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        /**
         * button register: call register function
         */
        button_register.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                final String email = edit_email.getText().toString();
                final String password = edit_password.getText().toString();
                String tmpNickname = edit_nickname.getText().toString();
                final String nickname = (tmpNickname == "" || tmpNickname == " ")?
                        email.substring(1, email.indexOf("@")):tmpNickname;
                createUser(email, nickname, password);
            }
        });

        /**
         * check box: show password clear text
         */
        box_showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){ // show the password clear text
                        edit_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                    else{ // hide the password
                        edit_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            }
        );
    }

    /**
     * login function: check if user sign in with a legal account
     */
    public void login(View v){
        final String email = edit_email.getText().toString();
        final String password = edit_password.getText().toString();
        String tmpNickname = edit_nickname.getText().toString();
        final String nickname = (tmpNickname == "" || tmpNickname == " ")?
                email.substring(1, email.indexOf("@")):tmpNickname;
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            register(email, nickname, password);
                        }
                        else{
                            gotoDisplayChatMsg();
                        }
                    }
                });
    }

    /**
     * register function: show error message and ask user if they want to create accounts
     */
    private void register(final String email, final String nickname, final String password){
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Login problem")
                .setMessage("Account doesn't exist. Do you want to register an account with this email? " + email)
                .setPositiveButton("Register",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createUser(email, nickname, password);
                            }
                        })
                .setNeutralButton("Cancel", null)
                .show();
    }

    /**
     * createUser function: register a new user
     */
    private void createUser(final String email, final String nickname, final String password){

        Toast.makeText(LoginActivity.this,"create user",Toast.LENGTH_LONG).show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String message;
                                if (task.isSuccessful()){
                                    message = "Register OK";
                                    //int[] androidColors = getResources().getIntArray(R.array.colors);
                                    //int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
                                    //tmpColor = Integer.toString(randomAndroidColor);
                                    /* set Firebase */
                                    //Firebase.setAndroidContext(LoginActivity.this);
                                    /* set Firebase url */
                                    //url = "https://chatroom-e531d.firebaseio.com/";
                                    //mRef = new Firebase(url);
                                    /* Get the root of Firebase Database */
                                    //mDatabase = FirebaseDatabase.getInstance();
                                    //userRef = mDatabase.getReference("userData");
                                    /*userRef.push().setValue(new UserData(
                                            nickname,
                                            email,
                                            tmpColor));*/
                                    // Go to MainActivity, display chat message
                                    gotoDisplayChatMsg();
                                }
                                else{
                                    FirebaseAuthException e = (FirebaseAuthException )task.getException();
                                    message = "Register fail: "+e.getMessage();
                                    // End the app
                                    finish();
                                }
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage(message)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        }
                );
    }

    private void gotoDisplayChatMsg() {
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        /* set Firebase */
        //Firebase.setAndroidContext(LoginActivity.this);
        /* set Firebase url */
        //url = "https://chatroom-e531d.firebaseio.com/";
        //mRef = new Firebase(url);
        /* Get the root of Firebase Database */
        //mDatabase = FirebaseDatabase.getInstance();
        //userRef = mDatabase.getReference("userData");
        /* Query userData using the email from current user */
        /*queryUserRef = userRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail().toString());
        queryUserRef.addChildEventListener(new ChildEventListener() {
            String getNickname;
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                UserData userData = dataSnapshot.getValue(UserData.class);
                getNickname = userData.getUsername();
                intent.putExtra("nickname", getNickname);
                //Toast.makeText(LoginActivity.this,getNickname,Toast.LENGTH_LONG).show();

            }

            @Override
            public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                UserData userData = dataSnapshot.getValue(UserData.class);
                getNickname = userData.getUsername();
                intent.putExtra("nickname", getNickname);
                //Toast.makeText(LoginActivity.this,getNickname,Toast.LENGTH_LONG).show();

            }

            @Override
            public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        // Go to MainActivity, display chat message
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }

    private void Login_ShowLayout() {
        text_title = findViewById(R.id.text_title);
        text_email = findViewById(R.id.text_email);
        text_nickname = findViewById(R.id.text_nickname);
        text_password = findViewById(R.id.text_passwd);
        edit_email = findViewById(R.id.edit_email);
        edit_nickname = findViewById(R.id.edit_nickname);
        edit_password = findViewById(R.id.edit_passwd);
        button_login = findViewById(R.id.button_login);
        button_register = findViewById(R.id.button_register);
        box_showPassword = findViewById(R.id.checkBox_showPassword);
    }
}
