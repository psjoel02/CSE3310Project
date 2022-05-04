package com.example.cse3310project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText ConfirmPassword;
    private TextInputLayout userLayout, emailLayout, passLayout;
    public Button registerButton;
    public TextView loginTV;
    private FirebaseAuth mAuth;
    private ProgressBar loadingBar;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText userEdit = findViewById(R.id.idEditUser);
        TextInputEditText emailEdit = findViewById(R.id.idEditEmail);
        TextInputEditText passwordEdit = findViewById(R.id.idEditPassword);
        ConfirmPassword = findViewById(R.id.idEditConfirmPassword);
        registerButton = findViewById(R.id.idRegisterButton);
        loadingBar = findViewById(R.id.idProgressBarLoad);
        loginTV = findViewById(R.id.idTVLogin);
        userLayout = findViewById(R.id.idTempUser);
        emailLayout = findViewById(R.id.idTempEmail);
        passLayout = findViewById(R.id.idTempPassword);
        mAuth = FirebaseAuth.getInstance();

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            emailEdit.setText(extra.getString("email"));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            //set notification format for email verification
        }

        registerButton.setOnClickListener(v -> register());

        loginTV.setOnClickListener(view -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });


    }

    protected void register(){
        loadingBar.setVisibility(View.VISIBLE);
        String username = Objects.requireNonNull(userLayout.getEditText()).getText().toString().trim();
        String email = Objects.requireNonNull(emailLayout.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(passLayout.getEditText()).getText().toString().trim();
        String confirmPassword = Objects.requireNonNull(ConfirmPassword.getText()).toString().trim();
        String fullname = "";
        String dob = "";
        String verifiedstatus = "not verified";

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("password", password);
        userMap.put("fullname", fullname);
        userMap.put("dob", dob);
        userMap.put("verifiedstatus", verifiedstatus);

        if(!password.equals(confirmPassword)){
            Toast.makeText(RegisterActivity.this,"Passwords must match" ,Toast.LENGTH_SHORT).show();
            //if password does not match, show toast
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if((TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)) || ((TextUtils.isEmpty(username)) || (TextUtils.isEmpty(email)))) {
            if(TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterActivity.this,"Enter your information" ,Toast.LENGTH_SHORT).show();
                //if email, username, password, and confirm password fields are empty, toast to prompt entry
            } else {
                // Alert user in the case that email is not empty, but password fields are
                Toast.makeText(RegisterActivity.this, "Enter a password", Toast.LENGTH_SHORT).show();
            }
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if(!email.contains("@") || !email.contains(".")){
            Toast.makeText(RegisterActivity.this, "Email is not formatted correctly", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            //if email does not contain @ or ., it will not be accepted
        }
        else if(password.length() < 6 || username.length() < 6){
            Toast.makeText(RegisterActivity.this, "Password or username is not greater than 6 characters", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            //password must be greater than 6 characters, no alphanumeric required
        }
        else if(password.length() > 20 || username.length() > 20){
            Toast.makeText(RegisterActivity.this, "Password or username is greater than 20 characters", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            //password must be less than 20 characters, no alphanumeric required
        }
        else{
            Query checkUser = root.orderByChild("username").equalTo(username);
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                            /*check if username exists in FireBase Realtime Database, if not, and email also
                            does not exist in Firebase Authentication, user will be created in both Auth and RTDB*/
                        Toast.makeText(RegisterActivity.this,"Username already exists in DB" ,Toast.LENGTH_SHORT).show();
                        loadingBar.setVisibility(View.INVISIBLE);
                    }
                    else{
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){

                                Objects.requireNonNull(mAuth.getCurrentUser()).sendEmailVerification().addOnSuccessListener(unused -> {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(RegisterActivity.this, "My Notification");
                                    builder.setContentTitle("AIR Reminder");
                                    builder.setContentText("A verification email was sent to the address specified.");
                                    builder.setSmallIcon(R.drawable.ic_shiba);
                                    builder.setAutoCancel(true);
                                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(RegisterActivity.this);
                                    managerCompat.notify(1,builder.build());
                                    //send notification to user to remind them to verify their email
                                });

                                root.push().setValue(userMap);
                                //upload data to database and simultaneously create user with email and password in auth
                                loadingBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                //if authorization with firebase is successful, register user and move to home class
                                startActivity(i);
                                finish();
                            }
                            else{
                                loadingBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegisterActivity.this,"Registration Failed, " + Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_LONG).show();
                                //if authorization with firebase is not successful, notify user of fail, along with the reason
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RegisterActivity.this,"Registration Failed, ",Toast.LENGTH_LONG).show();
                }
            });
        }

    }

}