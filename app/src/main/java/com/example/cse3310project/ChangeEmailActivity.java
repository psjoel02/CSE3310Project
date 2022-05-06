package com.example.cse3310project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangeEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
    private TextInputLayout oldEmailLayout, newEmailLayout, confirmNewEmailLayout;
    private ProgressBar loadingBar;
    Button homeButton, changeEmailButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CSE3310Project);
        setContentView(R.layout.activity_change_email);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        TextInputEditText password = findViewById(R.id.idPassword);
        TextInputEditText newemail = findViewById(R.id.idNewEmail);
        TextInputEditText confirmNewEmail = findViewById(R.id.idConfirmNewEmail);
        homeButton = findViewById(R.id.idHomeButton);
        changeEmailButton = findViewById(R.id.idChangeEmailButton);
        loadingBar = findViewById(R.id.idProgressBarLoad);
        oldEmailLayout = findViewById(R.id.idTempOldEmail);
        newEmailLayout = findViewById(R.id.idTempNewEmail);
        confirmNewEmailLayout = findViewById(R.id.idTempConfirmNewEmail);


        changeEmailButton.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            String Password = Objects.requireNonNull(password.getText()).toString().trim();
            String Email =  Objects.requireNonNull(newemail.getText()).toString().trim();
            String confirmEmail = Objects.requireNonNull(confirmNewEmail.getText()).toString().trim();
            changeEmail(Password, Email, confirmEmail, currentUser);
            loadingBar.setVisibility(View.INVISIBLE);
        });

        homeButton.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(ChangeEmailActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            loadingBar.setVisibility(View.VISIBLE);
        });

    }

    protected void changeEmail(String Password, String Email, String confirmEmail, FirebaseUser currentUser){
        if(!Email.equals(confirmEmail)){
            Toast.makeText(ChangeEmailActivity.this,"Emails must match" ,Toast.LENGTH_SHORT).show();
            //if password does not match, show toast
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if((TextUtils.isEmpty(Email) || TextUtils.isEmpty(confirmEmail)) || TextUtils.isEmpty(Password)) {
            Toast.makeText(ChangeEmailActivity.this,"Enter your information" ,Toast.LENGTH_SHORT).show();
            //if no information is entered, show toast
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if(!Email.contains("@") || !Email.contains(".")){
            Toast.makeText(ChangeEmailActivity.this, "Email is not formatted correctly", Toast.LENGTH_SHORT).show();
            //if email does not contain @ or . chars, show toast
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if(Email.length() < 10 || Email.length() > 35){
            Toast.makeText(ChangeEmailActivity.this, "Length must be greater than 10 and less than 35", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            //password must be greater than 6 characters, no alphanumeric required
        }
        else{
            AppCompatActivity activity = this;
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), Password);
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                //reauthentication successful
                if(task.isSuccessful()){
                    currentUser.verifyBeforeUpdateEmail(Email).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            root.orderByChild("password").equalTo(Password).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String currentPassword = ds.child("password").getValue(String.class);
                                            if (currentPassword != null) {
                                                if (currentPassword.equals(Password)) {
                                                    ds.child("email").getRef().setValue(Email);
                                                    //using password to access DB, change email only if new email is verified

                                                    // Change email associated with account
                                                    currentUser.updateEmail(Email);
                                                }
                                                else {
                                                    //current user does not match FBRTDB
                                                    Toast.makeText(ChangeEmailActivity.this, "Could not find user ", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                        }
                                    }
                                    else {
                                        //snapshot does not exist
                                        Toast.makeText(ChangeEmailActivity.this, "Snapshot of DB does not exist ", Toast.LENGTH_LONG).show();
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ChangeEmailActivity.this, "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
                                }
                            });
                            ProfileFragment changeEmail = new ProfileFragment();
                            changeEmail.setProf_email(Email);
                            Toast.makeText(ChangeEmailActivity.this, "Email updated.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ChangeEmailActivity.this, "Not updated, task failed"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    Toast.makeText(ChangeEmailActivity.this, "Not updated, task failed"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            // Sign the current user out, then return to login page
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ChangeEmailActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
