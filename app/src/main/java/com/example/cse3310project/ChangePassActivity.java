package com.example.cse3310project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import org.w3c.dom.Text;

import java.util.Objects;

public class ChangePassActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
    private TextInputLayout oldPassLayout, newPassLayout, confirmNewPassLayout;
    private ProgressBar loadingBar;
    Button homeButton, changePassButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CSE3310Project);
        setContentView(R.layout.activity_change_pass);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        TextInputEditText oldPass = findViewById(R.id.idOldPass);
        TextInputEditText newPass = findViewById(R.id.idNewPass);
        TextInputEditText confirmNewPass = findViewById(R.id.idConfirmNewPass);
        homeButton = findViewById(R.id.idHomeButton);
        changePassButton = findViewById(R.id.idChangePassButton);
        loadingBar = findViewById(R.id.idProgressBarLoad);
        oldPassLayout = findViewById(R.id.idTempOldPass);
        newPassLayout = findViewById(R.id.idTempNewPass);
        confirmNewPassLayout = findViewById(R.id.idTempConfirmNewPass);


        changePassButton.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            String oldPassword = Objects.requireNonNull(oldPass.getText()).toString().trim();
            String password = Objects.requireNonNull(newPass.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(confirmNewPass.getText()).toString().trim();
            ChangePassActivity.this.changePassword(oldPassword, password, confirmPassword, currentUser);
            loadingBar.setVisibility(View.INVISIBLE);
        });

        homeButton.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(ChangePassActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            loadingBar.setVisibility(View.VISIBLE);
        });

    }

    protected void changePassword(String oldPassword, String password, String confirmPassword, FirebaseUser currentUser){
        if(!password.equals(confirmPassword)){
            Toast.makeText(ChangePassActivity.this,"Passwords must match" ,Toast.LENGTH_SHORT).show();
            //if password does not match, show toast
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if((TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) || TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(ChangePassActivity.this,"Enter your information" ,Toast.LENGTH_SHORT).show();
            //"Enter your information"
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else if(password.length() < 6 || password.length() > 20){
            Toast.makeText(ChangePassActivity.this, "Length must be greater than 6 and less than 20", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            //password must be greater than 6 characters, no alphanumeric required
        }
        else{
           AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), oldPassword);
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                //reauthentication successful
                if(task.isSuccessful()){
                    currentUser.updatePassword(password).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            root.orderByChild("email").equalTo(currentUser.getEmail()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String currentEmail = ds.child("email").getValue(String.class);
                                            if (currentEmail != null) {
                                                if (currentEmail.equals(currentUser.getEmail())) {
                                                    ds.child("password").getRef().setValue(password);
                                                }
                                                else {
                                                    //current user does not match FBRTDB
                                                    Toast.makeText(ChangePassActivity.this, "Could not find user ", Toast.LENGTH_LONG).show();
                                                }
                                            }

                                        }
                                    }
                                    else {
                                        //snapshot does not exist
                                        Toast.makeText(ChangePassActivity.this, "Snapshot of DB does not exist", Toast.LENGTH_LONG).show();
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(ChangePassActivity.this, "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
                                }
                            });
                            ProfileFragment changePass = new ProfileFragment();
                            changePass.setProf_password(password);
                            Toast.makeText(ChangePassActivity.this, "Password updated.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(ChangePassActivity.this, "Not updated, task failed"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    Toast.makeText(ChangePassActivity.this, "Not updated, task failed"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Intent intent = new Intent(ChangePassActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
