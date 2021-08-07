package com.example.adminapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adminapplication.Login;
import com.example.adminapplication.MainActivity;
import com.example.adminapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    Button mForgotText, mDeleteText;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String accountEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mForgotText = findViewById(R.id.resetPassword);
        mDeleteText = findViewById(R.id.deleteAccount);
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        accountEmail = intent.getStringExtra("Account Email");



        String userID = fAuth.getCurrentUser().getUid();

        mForgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText resetMail = new EditText(v.getContext());
                resetMail.setText(accountEmail);
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Reset Link Will Be Sent To This Email.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and send reset link

                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SettingsActivity.this, "Reset Link Sent To Your Email.",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "Error. Reset Link Was Not Sent." + e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();
            }
        });

        mDeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText mPassword  = new EditText(v.getContext());

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Delete Account Password?");
                passwordResetDialog.setMessage("Enter Your Password And Click Delete");

                passwordResetDialog.setView(mPassword);

                passwordResetDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mPassword.setError(null);
                        String password = mPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(password)){
                            mPassword.setError("Please Enter Your Password.");
                            return;
                        }
                        //extract email and send reset link

                        fAuth.signInWithEmailAndPassword(accountEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    fstore.collection("admins").document(userID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            fAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                                    fAuth.signOut();
                                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {

                                                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }else {
                                    Toast.makeText(SettingsActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    //       progressBar.setVisibility(View.GONE);
                                }
                            }
                        });


                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();
            }
        });


    }







    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}