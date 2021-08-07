package com.example.adminapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView mFirstName, mId;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseMessaging firebaseMessaging;
    String userID, accountEmail, accountID, accountFirstName, accountLastName;
    ImageButton mEditProfile, mSettings;

    Button  singleUser, bulkUser;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        System.out.println(FirebaseAuth.getInstance().getCurrentUser()+"-----------3");

        mFirstName = findViewById(R.id.id);
        mEditProfile = findViewById(R.id.editProfile);
        mSettings = findViewById(R.id.settings);
        singleUser = findViewById(R.id.singleUser);
        bulkUser = findViewById(R.id.bulkUser);
        mId = findViewById(R.id.mId);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();


        userID = fAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("admins").document(userID);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {

            if (error != null) {
                Log.w("TAG", "Listen failed.", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                accountEmail = documentSnapshot.getString("Email");
                accountID = documentSnapshot.getString("Admin_ID");
                accountFirstName = documentSnapshot.getString("First_Name");
                accountLastName = documentSnapshot.getString("Last_Name");
                mFirstName.setText(accountFirstName);
                mId.setText(accountID);




                firebaseMessaging.getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();
                            Map<String,Object> user = new HashMap<>();
                            user.put("FCM_Token",token);
                            documentReference.set(user, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("tag", "OnSuccess: Main Activity token added"));
                        });
            } else {
                Log.d("TAG", "Current data: null");
            }





        });

        //go to edit profile
        mEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            intent.putExtra("Account Email", accountEmail);
            intent.putExtra("Admin ID", accountID);
            intent.putExtra("First Name", accountFirstName);
            intent.putExtra("Last Name", accountLastName);
            startActivity(intent);
            finish();
        });

        //go to settings
        mSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("Account Email", accountEmail);
            startActivity(intent);
            finish();
        });

        // go to single user
        singleUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CheckSingleUser.class);
            startActivity(intent);
            finish();
        });

        // go to bulk users
        bulkUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BulkCard.class);
            startActivity(intent);
            finish();
        });


    }




    public void logout(View view){
        String userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        DocumentReference documentReference = fStore.collection("admins").document(userID);
        Map<String,Object> user = new HashMap<>();
        user.put("FCM_Token","token");
         documentReference.set(user, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("tag", "OnSuccess: Logout token removed"));

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }




}