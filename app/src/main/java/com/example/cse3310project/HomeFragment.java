package com.example.cse3310project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private String name;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
    private ProgressBar loadingBar;
    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView helloTextView = view.findViewById(R.id.helloTextView);
        TextView verifiedTV = view.findViewById(R.id.verifiedTV);
        Button uploadFromGalleryButton = view.findViewById(R.id.uploadFromGalleryButton);
        Button uploadFromCameraButton = view.findViewById(R.id.uploadFromCameraButton);
        Button signOutButton = view.findViewById(R.id.signOutButton);
        Button securityQuestions = view.findViewById(R.id.securityQuestionsButton);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        loadingBar = view.findViewById(R.id.idProgressBarHome);
        FragmentActivity activity = requireActivity();

        signOutButton.setOnClickListener(v -> {
            loadingBar.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(activity, LoginActivity.class);
            startActivity(i);
            Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show();
            loadingBar.setVisibility(View.INVISIBLE);
            activity.finish();
        });

        securityQuestions.setOnClickListener(v ->{
            loadingBar.setVisibility(View.VISIBLE);
            Intent i = new Intent(activity, SecurityQuestionsActivity.class);
            startActivity(i);
            loadingBar.setVisibility(View.INVISIBLE);
            activity.finish();
        });

        if(currentUser == null) {
            Intent i = new Intent(activity, LoginActivity.class);
            Toast.makeText(activity, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(i);
            activity.finish();
            //log user out if not signed in
        }

        if(!Objects.requireNonNull(currentUser).isEmailVerified()){
            verifiedTV.setVisibility(View.VISIBLE);
        }
        else{
            //if user is verified, update value in database and display verification status
            root.orderByChild("email").equalTo(currentUser.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String currentEmail = ds.child("email").getValue(String.class);
                            String verifiedstatus = ds.child("verifiedstatus").getValue(String.class);

                            if (Objects.requireNonNull(currentEmail).equals(currentUser.getEmail()) && verifiedstatus != null) {
                                ds.child("verifiedstatus").getRef().setValue("verified");
                                verifiedTV.setText("You are verified");
                                verifiedTV.setVisibility(View.VISIBLE);
                            }
                            else{
                                //current user does not match FBRTDB
                                //Toast.makeText(getContext(), "Login Failed," + currentUser.getEmail(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else{
                        //snapshot does not exist
                        Toast.makeText(getContext(), "Login Failed, snapshot error", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Login Failed, database error ", Toast.LENGTH_LONG).show();
                }
            });
        }

        //SharedPreferences stores name on device for homepage, profile reads from DB
        SharedPreferences mPrefs = requireContext().getSharedPreferences("NamePref", 0);
        name = mPrefs.getString("HomeName", "UserNotFound");
        helloTextView.setText(String.format("Hello, %s!", name));

        uploadFromCameraButton.setOnClickListener(view2 -> {
            // Open camera activity
            Intent i = new Intent(activity, ImageUploadActivity.class);
            i.putExtra("launch", "camera");
            startActivity(i);
        });

        uploadFromGalleryButton.setOnClickListener(v -> {
            // Open gallery activity
            Intent i = new Intent(activity, ImageUploadActivity.class);
            i.putExtra("launch", "gallery");
            startActivity(i);
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}

