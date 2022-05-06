package com.example.cse3310project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ForgotPassActivity extends AppCompatActivity {

    private String question1, question2, answer1, answer2;
    private TextInputLayout answer1Layout, answer2Layout;
    private FirebaseAuth mAuth;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
    Button loginButton, resetPassButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        setTheme(R.style.Theme_CSE3310Project);
        mAuth = FirebaseAuth.getInstance();
        //if any of the questions or answers are null, password cannot be reset...
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        question1 = preferences.getString("question1", "");
        question2 = preferences.getString("question2", "");
        answer1 = preferences.getString("answer1", "");
        answer2 = preferences.getString("answer2", "");
        TextView Q1Text = findViewById(R.id.Q1Text);
        TextView Q2Text = findViewById(R.id.Q2Text);
        Q1Text.setText(question1);
        Q2Text.setText(question2);
        TextInputEditText answer1Text = findViewById(R.id.idAnswer1);
        TextInputEditText answer2Text = findViewById(R.id.idAnswer2);
        answer1Layout = findViewById(R.id.idTempAnswer1);
        answer2Layout = findViewById(R.id.idTempAnswer2);
        loginButton = findViewById(R.id.idLoginButton);
        resetPassButton = findViewById(R.id.idResetPassButton);
        Intent i = getIntent();
        String email = i.getExtras().getString("email");
        if((question1 == null || question2 == null) || (answer1 == null || answer2 == null)) {
            Toast.makeText(ForgotPassActivity.this, "Your security questions and answers were not set.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ForgotPassActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        resetPassButton.setOnClickListener(view -> {
            if (Objects.requireNonNull(answer1Text.getText()).toString().equals("") || Objects.requireNonNull(answer2Text.getText()).toString().equals("")) {
                Toast.makeText(ForgotPassActivity.this, "Please enter your answers", Toast.LENGTH_SHORT).show();
            } else if (!answer1Text.getText().toString().equals(answer1)) {
                Toast.makeText(ForgotPassActivity.this, "Your first answer is incorrect", Toast.LENGTH_SHORT).show();
            } else if (!answer2Text.getText().toString().equals(answer2)) {
                Toast.makeText(ForgotPassActivity.this, "Your second answer is incorrect", Toast.LENGTH_SHORT).show();
            } else if (answer1Text.getText().toString().equals(answer1) && answer2Text.getText().toString().equals(answer2)) {
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
                }).addOnFailureListener(e -> Toast.makeText(ForgotPassActivity.this, "Password reset failed.", Toast.LENGTH_LONG).show());
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> Toast.makeText(ForgotPassActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()).
                        addOnFailureListener(e -> Toast.makeText(ForgotPassActivity.this, "Password reset link failed..." + e.getMessage(), Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(ForgotPassActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ForgotPassActivity.this, "Please enter your answers", Toast.LENGTH_SHORT).show();
            }
        });

    }
}