package com.example.cse3310project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class HomeActivity extends AppCompatActivity {

    private Bundle results;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Animal Identifier: Home");

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(navListener);

        selectFragment(new HomeFragment());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            Intent i = new Intent(this, LoginActivity.class);
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(i);
            finish();
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            String home_username = bundle.getString("username");
            String home_password = bundle.getString("password");
            String home_email = bundle.getString("email");
            String home_fullname = bundle.getString("fullname");
            String home_dob = bundle.getString("dob");
            ProfileFragment fragment = new ProfileFragment();
            fragment.setProf_username(home_username);
            fragment.setProf_password(home_password);
            fragment.setProf_email(home_email);
            fragment.setProf_fullname(home_fullname);
            fragment.setProf_dob(home_dob);
        }
        else if(bundle == null && currentUser != null){
            root.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String DBUser = ds.child("username").getValue(String.class);
                            String DBPass = ds.child("password").getValue(String.class);
                            String DBEmail = ds.child("email").getValue(String.class);
                            String DBName = ds.child("fullname").getValue(String.class);
                            String DBDob = ds.child("dob").getValue(String.class);

                            if(Objects.requireNonNull(DBEmail).equals(currentUser.getEmail())){
                                ProfileFragment fragment = new ProfileFragment();
                                fragment.setProf_username(DBUser);
                                fragment.setProf_password(DBPass);
                                fragment.setProf_email(DBEmail);
                                fragment.setProf_fullname(DBName);
                                fragment.setProf_dob(DBDob);
                            }

                        }
                    }
                    else{
                        //snapshot does not exist
                        Toast.makeText(HomeActivity.this, "Login Failed, snapshot error", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Login Failed, database error ", Toast.LENGTH_LONG).show();
                }
            });
            //Toast.makeText(this, "User successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Error transferring DB data test", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selected;
        switch(item.getItemId()) {
            case R.id.nav_profile:
                selected = new ProfileFragment();
                break;
            case R.id.nav_home:
            default:
                selected = new HomeFragment();
                break;
            // Can add more pages in the future
        }

        selectFragment(selected);
        return true;
    };

    private void selectFragment(Fragment selected) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
    }
}