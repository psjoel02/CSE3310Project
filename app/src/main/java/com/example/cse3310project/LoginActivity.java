package com.example.cse3310project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static boolean loggedIn;
    private TextInputLayout usernameLayout, passwordLayout;
    public Button loginButton;
    public TextView registerTV, forgotpassTV;
    private ProgressBar loadingBar;
    private FirebaseAuth mAuth;
    CheckBox rememberMe;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CSE3310Project);
        setContentView(R.layout.activity_login);
        TextInputEditText userEdit = findViewById(R.id.idEditUser);
        TextInputEditText passwordEdit = findViewById(R.id.idEditPassword);
        loginButton = findViewById(R.id.idLoginButton);
        registerTV = findViewById(R.id.idTVRegister);
        forgotpassTV = findViewById(R.id.idTVforgotPassword);
        loadingBar = findViewById(R.id.idProgressBarLoad);
        usernameLayout = findViewById(R.id.idTempUser);
        passwordLayout = findViewById(R.id.idTempPassword);
        rememberMe = findViewById(R.id.RememberMe);

        loggedIn = false;
        mAuth = FirebaseAuth.getInstance();

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            userEdit.setText(extra.getString("username"));
            //if user has logged in, set profile text to username in DB
        }
        forgotpassTV.setOnClickListener(view -> forgotPass());

        rememberMe.setOnCheckedChangeListener((compoundButton, b) -> {
            if(compoundButton.isChecked()){
                SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rememberme","true");
                editor.apply();
                //if checkbox is set to true, user will be automatically logged in, and it is true by default
            }
            else if(!compoundButton.isChecked()){
                SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rememberme","false");
                editor.apply();
            }
        });

        registerTV.setOnClickListener(view -> {
            loadingBar.setVisibility(View.VISIBLE);
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            //if registration button is clicked, move to registration class
            startActivity(i);
            loadingBar.setVisibility(View.INVISIBLE);
        });

        loginButton.setOnClickListener(view -> login());

    }


    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = prefs.getString("rememberme", "");
        if(!checkbox.equals("true") && user != null){
            //Toast.makeText(LoginActivity.this, "not checked", Toast.LENGTH_SHORT).show();
        }
        else if(checkbox.equals("true") && user != null){
            //Toast.makeText(LoginActivity.this, "Remember Me checked", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            this.finish();
        }
        else{
            //Toast.makeText(LoginActivity.this, "Please sign in", Toast.LENGTH_SHORT).show();
        }
    }

    protected void login(){
        //display loading bar when logging while authorizing with firebase and DB
        loadingBar.setVisibility(View.VISIBLE);
        String username = Objects.requireNonNull(usernameLayout.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Username or Password cannot be empty", Toast.LENGTH_SHORT).show();
            //if username or password is empty, toast message to enter info
            loadingBar.setVisibility(View.INVISIBLE);
        }
        else {
            root.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String currentUser = ds.child("username").getValue(String.class);
                            String currentPass = ds.child("password").getValue(String.class);

                            if (currentUser != null) {
                                if (currentPass != null) {
                                    if(currentUser.equals(username)){
                                        String currentEmail = ds.child("email").getValue(String.class);
                                        String currentName = ds.child("fullname").getValue(String.class);
                                        String currentDob = ds.child("dob").getValue(String.class);
                                        String currentImageurl = ds.child("imageurl").getValue(String.class);
                                        String verifiedstatus = ds.child("verifiedstatus").getValue(String.class);

                                        if (currentEmail != null) {
                                            mAuth.signInWithEmailAndPassword(currentEmail, password).addOnCompleteListener(task -> {
                                                if(task.isSuccessful()){
                                                    if(!password.equals(currentPass)){
                                                        /*if password was reset using forgot password, so DB is not same as FB
                                                        change DB password to password entered i.e. new password from firebase*/
                                                        ds.child("password").getRef().setValue(password);
                                                    }
                                                    loadingBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                                                    //if sign in with firebase is correct, notify user login is successful and go to homepage class
                                                    i.putExtra("username", currentUser);
                                                    i.putExtra("password", currentPass);
                                                    i.putExtra("email", currentEmail);
                                                    i.putExtra("fullname", currentName);
                                                    i.putExtra("dob", currentDob);
                                                    i.putExtra("imageurl", currentImageurl);
                                                    i.putExtra("verifiedstatus", verifiedstatus);
                                                    loggedIn = true;
                                                    startActivity(i);
                                                    finish();
                                                }
                                                else{
                                                    loadingBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Login Failed, task failed"+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                    else{
                                        //current user does not match FBRTDB
                                        loadingBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(LoginActivity.this, "Login Failed, current user not matched", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                        }
                    }
                    else{
                        //snapshot does not exist
                        loadingBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Login Failed, snapshot error", Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Login Failed, database error ", Toast.LENGTH_LONG).show();
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            });

        }
    }
    protected void forgotPass(){
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(LoginActivity.this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter your email");
        EditText currentEmail = new EditText(LoginActivity.this);
        LinearLayout reset =new LinearLayout(getBaseContext());
        reset.setOrientation(LinearLayout.VERTICAL);
        reset.addView(currentEmail);
        currentEmail.setHint("Your email");
        currentEmail.setHintTextColor(ContextCompat.getColor(getBaseContext(), R.color.blue3));
        passwordResetDialog.setView(reset);
        passwordResetDialog.setIcon(R.drawable.shiba);
        //create dialog to reset password if email and password match existing account in FB and DB
        passwordResetDialog.setPositiveButton(Html.fromHtml("<font color='#4287f5'>Confirm</font>"), (dialogInterface, i) -> root.orderByChild("email").equalTo(currentEmail.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String DBemail = ds.child("email").getValue(String.class);
                        String verifiedstatus = ds.child("verifiedstatus").getValue(String.class);
                        if (currentEmail.getText().toString().equals(DBemail) && Objects.requireNonNull(verifiedstatus).equals("verified")) {
                            String email = currentEmail.getText().toString();
                            //send email as intent to forgot password class
                            Intent intent = new Intent(LoginActivity.this, ForgotPassActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        }
                        else if (!Objects.requireNonNull(verifiedstatus).equals("verified")){
                            //current user does not match FBRTDB
                            Toast.makeText(LoginActivity.this, "Not verified", Toast.LENGTH_LONG).show();
                        }
                        else if(!currentEmail.getText().toString().equals(DBemail)){
                            Toast.makeText(LoginActivity.this, "Email not found", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Not verified or email not found", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else{
                    //snapshot does not exist
                    Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Login Failed, database error ", Toast.LENGTH_LONG).show();
            }
        }));

        passwordResetDialog.setNeutralButton(Html.fromHtml("<font color='#4287f5'>Exit</font>"), (dialogInterface, i) -> {
            //close dialog and don't send link
            dialogInterface.dismiss();
        });
        passwordResetDialog.create().show();
    }
}