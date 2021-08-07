package com.example.adminapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mId, mEmail, mPassword, mPasswordConfirm, mFirstName, mLastName;
    Button mRegisterBtn;
    TextView mLoginPage;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fstore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mId = findViewById(R.id.id);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password1);
        mPasswordConfirm = findViewById(R.id.password2);
        mRegisterBtn = findViewById(R.id.register);
        mLoginPage = findViewById(R.id.toLoginPage);
        mFirstName = findViewById(R.id.firstName);
        mLastName = findViewById(R.id.lastName);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);




        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mId.setError(null);
                mPassword.setError(null);
                mPasswordConfirm.setError(null);
                mEmail.setError(null);

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordConfirm = mPasswordConfirm.getText().toString().trim();
                String id = mId.getText().toString();
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                int balance = 0;

                if (TextUtils.isEmpty(firstName)){
                    mFirstName.setError("Please Enter Your First Name.");
                    return;
                }
                if (TextUtils.isEmpty(lastName)){
                    mLastName.setError("Please Enter Your Last Name.");
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Please Enter Your Email.");
                    return;
                }

                if (TextUtils.isEmpty(id)){
                    mId.setError("Please Enter Your Student ID.");
                    return;
                }
                if(id.length() < 8){
                    mId.setError("Please Enter A Valid ID.");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError("Please Enter A Password.");
                    return;
                }
                if (TextUtils.isEmpty(passwordConfirm)){
                    mPasswordConfirm.setError("Please Confirm Password.");
                    return;
                }
               /* if(password.length() < 6){
                    mPassword.setError("Password must be more than 6 characters.");
                    return;
                }*/
                if(!password.equals(passwordConfirm)){
                    mPasswordConfirm.setError("Two passwords do not match.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //REGISTER USER IN FIREBASE


                //LOOK FOR ID and Email IN DATABASE AND IF not EXISTS, edit profile
                CollectionReference users = fstore.collection("admins");
                Query query =  users.whereEqualTo("Admin_ID", id);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //if it doesnt exist
                            if(Objects.requireNonNull(task.getResult()).isEmpty()){

                                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){

                                            //verify email

                                            FirebaseUser fuser = fAuth.getCurrentUser();
                                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(Register.this, "Verification Email Has Been Sent", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "On Failure: Verification email not sent" + e.getMessage());
                                                }
                                            });



                                            Toast.makeText(Register.this, "Admin Created", Toast.LENGTH_SHORT).show();
                                            userID = fAuth.getCurrentUser().getUid();
                                            DocumentReference documentReference = fstore.collection("admins").document(userID);
                                            Map<String,Object> user = new HashMap<>();
                                            user.put("Admin_ID",id);
                                            user.put("Email",email);
                                            user.put("First_Name",firstName);
                                            user.put("Last_Name",lastName);
                                            user.put("Account_Type","Admin");
                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "OnSuccess: Profile created for " + userID);
                                                }
                                            });
                                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                        }else {
                                            Toast.makeText(Register.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);

                                        }
                                    }
                                });

                            }
                            else {
                                mId.setError("Account With This Admin ID Already Exists.");
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());

                        }
                    }
                });



            }
        });


        mLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));

            }
        });
    }
}