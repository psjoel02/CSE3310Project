package com.example.cse3310project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SecurityQuestionsActivity extends AppCompatActivity{

    Button homeButton, changeSQButton;
    TextView sq1Text, sq2Text;
    private Spinner sq1, sq2;
    private ProgressBar loadingBar;
    private String question1, question2;
    private int sqCount;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
    private static final String[] QuestionSet1 = {"In what city were you born?",
                                                 "What is the name of your favorite pet?",
                                                 "What is your mother's maiden name?",
                                                 "What was the name of the first school you attended?",
                                                 "What was your childhood nickname?"};

    private static final String[] QuestionSet2 = {"In what city did your parents meet?",
                                                  "What is your father's middle name?",
                                                  "What is your oldest sibling's middle name?",
                                                  "What high school did you attend?",
                                                  "What was the make and model of your first car?"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_questions);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        sq1 = findViewById(R.id.SecurityQ1);
        sq2 = findViewById(R.id.SecurityQ2);
        sq1Text = findViewById(R.id.sq1Text);
        sq2Text = findViewById(R.id.sq2Text);
        homeButton = findViewById(R.id.idHomeButton);
        changeSQButton = findViewById(R.id.idChangeSQButton);
        loadingBar = findViewById(R.id.idProgressBarLoad);
        sqCount = 0;
        ArrayAdapter<String>adapter1 = new ArrayAdapter<>(SecurityQuestionsActivity.this, android.R.layout.simple_spinner_item, QuestionSet1);
        ArrayAdapter<String>adapter2 = new ArrayAdapter<>(SecurityQuestionsActivity.this, android.R.layout.simple_spinner_item, QuestionSet2);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sq1.setAdapter(adapter1);
        sq2.setAdapter(adapter2);

        homeButton.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            Intent intent = new Intent(SecurityQuestionsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            loadingBar.setVisibility(View.VISIBLE);
        });

        changeSQButton.setOnClickListener(view ->{
            loadingBar.setVisibility(View.VISIBLE);
            sq1Text.setText(sq1Text.getText().toString());
            sq2Text.setText(sq2Text.getText().toString());
            root.orderByChild("email").equalTo(Objects.requireNonNull(currentUser).getEmail()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String currentEmail = ds.child("email").getValue(String.class);
                            if (currentEmail != null) {
                                if (currentEmail.equals(currentUser.getEmail())) {
                                    if(!question1.equals("") && !question2.equals("")) {
                                        ds.child("question1").getRef().setValue(question1);
                                        ds.child("question2").getRef().setValue(question2);
                                        //if question exists, set the user's question to the selected option, no limit on length
                                        if(!sq1Text.getText().toString().equals("") && !sq2Text.getText().toString().equals("")){
                                            if(!sq1Text.getText().toString().equals(sq2Text.getText().toString())){
                                                //check if same answer is present for both
                                                if (sqCount == 0) {
                                                    if(!sq1Text.getText().toString().equals(ds.child("answer1").getValue(String.class))){
                                                        ds.child("answer1").getRef().setValue(sq1Text.getText().toString());
                                                        //check if answer1 is same as database, if not, set value to answer1
                                                    }

                                                    if(!sq1Text.getText().toString().equals(ds.child("answer2").getValue(String.class))){
                                                        ds.child("answer2").getRef().setValue(sq2Text.getText().toString());
                                                        //check if answer1 is same as database, if not, set value to answer1
                                                    }
                                                    Toast.makeText(SecurityQuestionsActivity.this, "Security questions have been updated.", Toast.LENGTH_SHORT).show();
                                                }
                                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SecurityQuestionsActivity.this);
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("question1",question1);
                                                editor.putString("question2",question2);
                                                editor.putString("answer1",sq1Text.getText().toString());
                                                editor.putString("answer2", sq2Text.getText().toString());
                                                editor.apply();
                                                //set security questions and answers in security questions
                                                Intent intent = new Intent(SecurityQuestionsActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                                sqCount++;
                                            }
                                        }
                                        else{
                                            Toast.makeText(SecurityQuestionsActivity.this, "Security answers must not be empty.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        Toast.makeText(SecurityQuestionsActivity.this, "Security questions must not be empty.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    //current user does not match FBRTDB
                                    Toast.makeText(SecurityQuestionsActivity.this, "User not found", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    else {
                        //snapshot does not exist
                        Toast.makeText(SecurityQuestionsActivity.this, "Snapshot of DB does not exist(username) ", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SecurityQuestionsActivity.this, "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
                }
            });
            loadingBar.setVisibility(View.INVISIBLE);
        });

        sq1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selection, long l) {
                question1 = sq1.getSelectedItem().toString();
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLUE);
                //Toast.makeText(SecurityQuestionsActivity.this, question1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                question1 = sq1.getSelectedItem().toString();
            }
        });

        sq2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int selection, long l) {
                question2 = sq2.getSelectedItem().toString();
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLUE);
                //Toast.makeText(SecurityQuestionsActivity.this, question2, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                question2 = sq2.getSelectedItem().toString();
            }
        });

    }

}