package com.example.cse3310project;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    TextView userProfileHeader, fullNameText, dateOfBirthText, userText, emailText, passText, FBUpdate;
    public static String prof_username, prof_password, prof_email, prof_dob, prof_fullname = null;
    Button deleteAccountButton, updateButton;
    private ProgressBar loadingBar;
    private ImageView profilePic;
    private int profileCount, choice;
    final Calendar myCalendar= Calendar.getInstance();
    DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FragmentActivity activity = requireActivity();
        profileCount = 0; //prevent toast from reappearing
        choice = -1; //default profile picker choice
        userProfileHeader = view.findViewById(R.id.userProfileHeaderText);
        fullNameText = view.findViewById(R.id.userFullNameText);
        userText = view.findViewById(R.id.userNameText);
        emailText = view.findViewById(R.id.emailText);
        passText = view.findViewById(R.id.passText);
        emailText.setEnabled(false); //user cannot edit email or password directly from profile
        passText.setEnabled(false);
        FBUpdate = view.findViewById(R.id.FBUpdate);
        loadingBar = view.findViewById(R.id.idProgressBarProf);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        updateButton = view.findViewById(R.id.updateButton);
        dateOfBirthText = view.findViewById(R.id.userDateOfBirthText);
        profilePic = view.findViewById(R.id.profile_image);

        if (currentUser == null) {
            Intent i = new Intent(activity, LoginActivity.class);
            Toast.makeText(activity, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(i);
            requireActivity().finish();
            return;
        }
        else if(currentUser != null && currentUser.getPhotoUrl() != null){
                //profilePic.setRotation(0);
                //on my phone it shows up as rotated -90 degrees when taking photographs,
                //using the emulator it shows up as rotated 90 degrees, can be modified before presentation
                Glide.with(getActivity()).load(currentUser.getPhotoUrl()).into(profilePic);
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  final Intent[] galleryIntent = {null};
                  AlertDialog.Builder imgPick = new AlertDialog.Builder(requireContext());
                  imgPick.setTitle("Choose camera or gallery");
                  imgPick.setIcon(R.drawable.shiba);
                  imgPick.setMessage("Pick a choice");
                  imgPick.setPositiveButton("Camera", (dialogInterface, i) -> {
                      galleryIntent[0] = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                      //pick profile pic from camera
                      choice = 0;
                      if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                          ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.CAMERA}, 101);
                          imageActivityResultLauncher.launch(galleryIntent[0]);
                      }
                      else{
                          Toast.makeText(getActivity(), "Camera permissions failed", Toast.LENGTH_SHORT).show();
                      }
                  }).setNeutralButton("Gallery", (dialogInterface, i) -> {
                      galleryIntent[0] = new Intent(String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                      //pick profile pic from gallery
                      if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                          galleryIntent[0].setType("image/*");
                          galleryIntent[0].setAction(Intent.ACTION_GET_CONTENT);

                          imageActivityResultLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
                      }
                      else{
                          Toast.makeText(getActivity(), "Gallery permissions failed", Toast.LENGTH_SHORT).show();
                      }
                  });
                  imgPick.create().show();
              }

              ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                  @Override
                  public void onActivityResult(ActivityResult result) {
                      if (result.getResultCode() == Activity.RESULT_OK && choice == 0) {
                          Intent data = result.getData();
                          Bitmap bmp = (Bitmap) data.getExtras().get("data");
                          Matrix rotationMatrix = new Matrix();
                          if(bmp.getWidth() >= bmp.getHeight()){
                              rotationMatrix.setRotate(90);
                          }else{
                              rotationMatrix.setRotate(0);
                          }

                          Bitmap rotatedBitmap = Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),rotationMatrix,true);
                          profilePic.setImageBitmap(rotatedBitmap);
                          uploadImage(rotatedBitmap);
                          //if camera was selected, convert data from onclick into bitmap and upload it to FB storage
                      }
                      else{
                          Bitmap bmp = null;
                          Uri data = null;
                          //catch cancel gallery error
                          if(result.getData() != null)
                             data = result.getData().getData();
                          else
                              return;
                          try {
                               bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data); //changes URI to bitmap
                               //if gallery was selected, get bitmap from gallery
                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                          profilePic.setImageBitmap(bmp);
                          uploadImage(bmp);
                      }
                  }
              });
          });

        DatePickerDialog.OnDateSetListener date = (view1, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH,month);
            myCalendar.set(Calendar.DAY_OF_MONTH,day);
            modifyDob();
        };
        dateOfBirthText.setOnClickListener(view12 -> new DatePickerDialog(getActivity(),date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        showProfileData();

        FBUpdate.setOnClickListener(v -> FBChoice());

        deleteAccountButton.setOnClickListener(v -> deleteAccount());

        showProfileData();
    }

    private void showProfileData() {

        userProfileHeader.setText(prof_username);
        fullNameText.setText(prof_fullname);
        dateOfBirthText.setText(prof_dob);
        userText.setText(prof_username);
        emailText.setText(prof_email);
        passText.setText(prof_password);

        updateButton.setOnClickListener(view -> updateProfileData());
    }


    public void updateProfileData() {
        if(isModified()){
            Toast.makeText(getActivity(), "Profile data has been updated.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isModified(){
        final boolean[] mod = {false};
        loadingBar.setVisibility(View.VISIBLE);
        root.orderByChild("email").equalTo(prof_email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String currentEmail = ds.child("email").getValue(String.class);
                        if (currentEmail != null) {
                            if (currentEmail.equals(prof_email)) {
                                ds.child("username").getRef().setValue(userText.getText().toString());
                                ds.child("fullname").getRef().setValue(fullNameText.getText().toString());
                                userProfileHeader.setText(userText.getText().toString());
                                userText.setText(userText.getText().toString());
                                fullNameText.setText(fullNameText.getText().toString());
                                setProf_username(userText.getText().toString());
                                setProf_fullname(fullNameText.getText().toString());
                                SharedPreferences mPrefs = getContext().getSharedPreferences("NamePref", 0);
                                SharedPreferences.Editor mEditor = mPrefs.edit();
                                mEditor.putString("HomeName", fullNameText.getText().toString()).apply();
                                mod[0] = true;
                                if(profileCount == 0) {
                                    Toast.makeText(getActivity(), "Profile data has been updated.", Toast.LENGTH_SHORT).show();
                                }
                                profileCount++;
                                    /*update profile data for username and name one at a time, must press update button to save changes
                                      or a crash could occur*/
                            }
                            else {
                                //current user does not match FBRTDB
                                Toast.makeText(getActivity(), "Could not find user ", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }
                else {
                    //snapshot does not exist
                    Toast.makeText(getActivity(), "Snapshot of DB does not exist(username) ", Toast.LENGTH_LONG).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
            }
        });

        profileCount = 0;
        loadingBar.setVisibility(View.INVISIBLE);
        return mod[0];
    }

    private void modifyDob(){
        loadingBar.setVisibility(View.VISIBLE);
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        dateOfBirthText.setText(dateFormat.format(myCalendar.getTime()));
        //use datepicker to choose value, then register it in RTDB
        root.orderByChild("username").equalTo(prof_username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String currentEmail = ds.child("email").getValue(String.class);

                        if (currentEmail != null) {
                            if (currentEmail.equals(prof_email)) {
                                ds.child("dob").getRef().setValue(dateOfBirthText.getText().toString());
                                dateOfBirthText.setText(dateOfBirthText.getText().toString());
                                setProf_dob(dateOfBirthText.getText().toString());
                            } else {
                                //current user does not match FBRTDB
                                Toast.makeText(getActivity(), "Could not find user ", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                }
                else {
                    //snapshot does not exist, shows up when update profile has
                    //Toast.makeText(getActivity(), "Snapshot of DB does not exist(dob) ", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
            }
        });
        loadingBar.setVisibility(View.INVISIBLE);
    }

    protected void FBChoice(){
        AlertDialog.Builder choiceDialog = new AlertDialog.Builder(requireContext());
        choiceDialog.setTitle("Change Email or Password");
        choiceDialog.setIcon(R.drawable.shiba);
        choiceDialog.setMessage("Pick a choice");
        //dialog displays choice to change email, password, or leave dialog
        choiceDialog.setPositiveButton("Email",
                (dialog, id) -> {
                    loadingBar.setVisibility(View.VISIBLE);
                    Intent i = new Intent(getActivity(), ChangeEmailActivity.class);
                    startActivity(i);
                    loadingBar.setVisibility(View.INVISIBLE);
                    requireActivity().finish();
                });

        choiceDialog.setNeutralButton("Exit",
                (dialog, id) -> dialog.cancel());

        choiceDialog.setNegativeButton("Password",
                (dialog, id) -> {
                    loadingBar.setVisibility(View.VISIBLE);
                    Intent i = new Intent(getActivity(), ChangePassActivity.class);
                    startActivity(i);
                    loadingBar.setVisibility(View.INVISIBLE);
                    requireActivity().finish();
                });
        choiceDialog.create().show();

    }

    protected void deleteAccount(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        AlertDialog.Builder choiceDialog = new AlertDialog.Builder(requireContext());
        choiceDialog.setTitle("Delete user data?");
        choiceDialog.setIcon(R.drawable.ic_baseline_warning_24);
        SpannableString msg = new SpannableString("Are you sure you want to delete your data?");
        SpannableString yes = new SpannableString("Yes, I am sure");
        SpannableString no = new SpannableString("No, go back");
        msg.setSpan(new ForegroundColorSpan(Color.GREEN), 0, msg.length(), 0);
        msg.setSpan(new StyleSpan(R.font.poppins_bold), 0, msg.length(), 0);
        yes.setSpan(new ForegroundColorSpan(Color.RED), 0, yes.length(), 0);
        yes.setSpan(new StyleSpan(R.font.poppins_bold), 0, yes.length(), 0);
        no.setSpan(new ForegroundColorSpan(Color.GREEN), 0, no.length(), 0);
        no.setSpan(new StyleSpan(R.font.poppins_bold), 0, no.length(), 0);
        choiceDialog.setMessage(msg);
        //dialog gives user the choice to delete their account's data
        final int[] DBsuccess = {-1};
        choiceDialog.setPositiveButton(yes,
                (dialog, id) -> {

                    root.orderByChild("username").equalTo(prof_username).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String currentEmail = ds.child("email").getValue(String.class);

                                    if (currentEmail != null) {
                                        if (currentEmail.equals(Objects.requireNonNull(currentUser).getEmail())) {
                                            ds.getRef().removeValue();
                                            DBsuccess[0] = 1;
                                        }
                                        else {
                                            //current user does not match FBRTDB
                                            Toast.makeText(getActivity(), "Could not find user ", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                            }
                            else {
                                //snapshot does not exist, shows up when update profile has
                                //Toast.makeText(getActivity(), "Snapshot of DB does not exist(dob) ", Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getActivity(), "Error accessing Realtime Database ", Toast.LENGTH_LONG).show();
                        }
                    });

                    Objects.requireNonNull(currentUser).delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && DBsuccess[0] > 0) {
                            Toast.makeText(getActivity(), "Account successfully deleted", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent i = new Intent(getActivity(), LoginActivity.class);
                            startActivity(i);
                            Toast.makeText(getActivity(), "Signed out", Toast.LENGTH_SHORT).show();
                            requireActivity().finish();
                        }
                        else{
                            Toast.makeText(getActivity(), "Account deletion failed", Toast.LENGTH_LONG).show();
                        }
                    });
                });

        choiceDialog.setNeutralButton(no,
                (dialog, id) -> dialog.dismiss());
        choiceDialog.create().show();
    }


    private void uploadImage(Bitmap bmp){
        ByteArrayOutputStream ByteArr = new ByteArrayOutputStream();
        try {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, ByteArr);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        StorageReference strref = FirebaseStorage.getInstance().getReference().child("profileImages").child(uid + ".jpeg");
        strref.putBytes(ByteArr.toByteArray()).addOnSuccessListener(taskSnapshot -> getImageUrl(strref)).addOnFailureListener(e -> Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void getImageUrl(StorageReference ref){
        ref.getDownloadUrl().addOnSuccessListener(this::setImageUrl);
    }

    private void setImageUrl(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        Objects.requireNonNull(user).updateProfile(request).addOnSuccessListener(unused -> {
            if(getActivity() != null)
                Toast.makeText(getActivity(), "Image succesfully uploaded", Toast.LENGTH_LONG).show();
                //user must wait to make sure the file is uploaded otherwise it may not sync with FB Storage immediately
        }).addOnFailureListener(e -> {
            if(getActivity() != null)
                Toast.makeText(getActivity(), "Image uploading failed", Toast.LENGTH_LONG).show();
        });
    }
    public void setProf_username(String str){
        prof_username = str;
    }
    public void setProf_password(String str){
        prof_password = str;
    }
    public void setProf_email(String str){
        prof_email = str;
    }
    public void setProf_dob(String str){
        prof_dob = str;
    }
    public void setProf_fullname(String str){
        prof_fullname = str;
    }

}