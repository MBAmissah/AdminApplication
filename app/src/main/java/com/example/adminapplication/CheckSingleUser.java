package com.example.adminapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;

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
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckSingleUser extends AppCompatActivity {

    Button searchBtn;
    EditText searchText;
    ProgressBar mProgressBar2;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    TextView recordFound, recordFound2, cardContentsText, listeningTextRead;
    ImageView tapCardGif;


    String accountEmail, accountFirstName, accountLastName;
    int accountBalance;

    boolean writePressed;
    View dividerItemDecoration;

    CardView cardViewCurrentStudent, cardView, tapCardtoReadCardView;
    private List<myUser> myUsers = new ArrayList<>();
    TextView currentStudent;




    //NFC STUFF
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static final String Error_Detected = "No NFC Tag Detected";
    public static final String Write_Success = "Text Written Successfully";
    public static final String Write_Error = "Error During Writing, Try Again";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writingTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView edit_message, nfc_content, cardIdText;
    Button writeToCard, cancelWriteToCard, eraseCardId;
    String mStudentId, mBalance2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_single_user);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        writePressed = false;


        searchBtn = findViewById(R.id.search);
        searchText = findViewById(R.id.searchId);


        recordFound = findViewById(R.id.recordFound);
        recordFound2 = findViewById(R.id.recordFound2);
        cardContentsText = findViewById(R.id.cardContentsText);
        dividerItemDecoration = findViewById(R.id.divider7);


        eraseCardId = findViewById(R.id.eraseCardId);
        tapCardtoReadCardView = findViewById(R.id.cardView);
        cardView = findViewById(R.id.cardView4);
        cardViewCurrentStudent = findViewById(R.id.cardViewCurrentStudent);
        currentStudent = findViewById(R.id.currentStudent);


        mProgressBar2 = findViewById(R.id.progressBar2);

//NFC STUFF

        nfc_content = (TextView) findViewById(R.id.cardContent);
        cardIdText = (TextView) findViewById(R.id.cardIdText);
        listeningTextRead = (TextView) findViewById(R.id.listeningTextRead);
        writeToCard = findViewById(R.id.writeToCard);
        cancelWriteToCard = findViewById(R.id.cancelWriteToCard);
        tapCardGif = findViewById(R.id.listening2);
        context = this;

        //search for user
        searchBtn.setOnClickListener(v -> {

            cardViewCurrentStudent.setVisibility(View.INVISIBLE);
            writeToCard.setVisibility(View.INVISIBLE);
            cancelWriteToCard.setVisibility(View.INVISIBLE);
            recordFound.setVisibility(View.INVISIBLE);

            searchText.setError(null);

            String id = searchText.getText().toString();

            if (TextUtils.isEmpty(id)){
                searchText.setError("Please Enter A Student ID.");
                return;
            }
            if(id.length() < 8){
                searchText.setError("ID Should Be 8 digits.");
                return;
            }

            mProgressBar2.setVisibility(View.VISIBLE);

            //LOOK FOR ID IN DATABASE AND IF EXISTS, display info
            CollectionReference users = fStore.collection("users");
            Query query =  users.whereEqualTo("Student_ID", id);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        mProgressBar2.setVisibility(View.INVISIBLE);

                        myUsers.clear();

                        //look for email in database
                        if(!Objects.requireNonNull(task.getResult()).isEmpty()){
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                myUsers.add(i, document.toObject(myUser.class));
                                i++;
                            }

                            recordFound.setVisibility(View.VISIBLE);
                            recordFound2.setVisibility(View.VISIBLE);
                            cardViewCurrentStudent.setVisibility(View.VISIBLE);
                            writeToCard.setVisibility(View.VISIBLE);
                            cancelWriteToCard.setVisibility(View.VISIBLE);
                            currentStudent.setText(myUsers.get(0).toString());

                            mBalance2 = String.valueOf(myUsers.get(0).getAccount_Balance());
                            mStudentId = myUsers.get(0).getStudent_ID();

                        }
                        else {
                            searchText.setError("Account With This Student ID Does Not Exist.");
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());

                    }
                }
            });

        });


        //erase student's card Id
        eraseCardId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //LOOK FOR user IN DATABASE AND update card id
                CollectionReference users = fStore.collection("users");
                Query query =  users.whereEqualTo("Student_ID", mStudentId);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //UPDATE RECIPIENT DATABASE ENTRY
                            if(!task.getResult().isEmpty()){


                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    DocumentReference documentReference = fStore.collection("users").document(document.getId());
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Card_Id", "");
                                    documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Card Erased", Toast.LENGTH_SHORT).show();

                                            Query query =  users.whereEqualTo("Student_ID", mStudentId);
                                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        mProgressBar2.setVisibility(View.INVISIBLE);

                                                        myUsers.clear();

                                                        //look for email in database
                                                        if(!Objects.requireNonNull(task.getResult()).isEmpty()){
                                                            int i = 0;
                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                myUsers.add(i, document.toObject(myUser.class));
                                                                i++;
                                                            }
                                                            currentStudent.setText(myUsers.get(0).toString());

                                                        }
                                                        else {
                                                            searchText.setError("Account With This Student ID Does Not Exist.");
                                                        }
                                                    } else {
                                                        Log.d("TAG", "Error getting documents: ", task.getException());

                                                    }
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(context, "Failed To Erase Card", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            else {
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());

                        }
                    }
                });

            }
        });


        // write onto card when button is pressed
        writeToCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePressed = true;
                cardView.setVisibility(View.VISIBLE);
                tapCardtoReadCardView.setVisibility(View.INVISIBLE);
            }
        });

        cancelWriteToCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePressed = false;
                cardView.setVisibility(View.GONE);
                tapCardtoReadCardView.setVisibility(View.VISIBLE);
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
        readfromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[]{tagDetected};

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




    private void readfromIntent(Intent intent){
        String action = intent.getAction();
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if(rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++){
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }


    @SuppressLint("SetTextI18n")
    private void buildTagViews(NdefMessage[] msgs){
        if(msgs == null || msgs.length == 0) {
            nfc_content.setText("NFC Card is Empty");
            return;
        }

        String text = "";
        String text2 = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        byte[] payload2 = msgs[0].getRecords()[1].getPayload();

        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; //GET TEXT ENCODING
        int languageCodeLength = payload[0] & 0063; //GET LANGUAGE CODE, e.g. "en
        String textEncoding2 = ((payload2[0] & 128) == 0) ? "UTF-8" : "UTF-16"; //GET TEXT ENCODING
        int languageCodeLength2 = payload2[0] & 0063; //GET LANGUAGE CODE, e.g. "en


        try{
            // get the text
            text = new String(payload, languageCodeLength+1,payload.length-languageCodeLength -1, textEncoding);
            text2 = new String(payload2, languageCodeLength2+1,payload2.length-languageCodeLength2 -1, textEncoding2);
        } catch (UnsupportedEncodingException e){
            Log.e("UnsupportedEncoding",e.toString());
        }

        nfc_content.setText("Account Balance: GH₵ " + text + "\nStudent ID: " + text2);
    }


    private void write(NdefMessage message, Tag tag) throws IOException, FormatException {
        //NdefRecord[] records = { createRecord(text)};

       // NdefMessage message = new NdefMessage(records);

        //get instance of Ndef for the tag.

        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            NdefFormatable formatable=NdefFormatable.get(tag);

            if (formatable != null) {
                try {
                    formatable.connect();

                    try {
                        formatable.format(message);
                    }
                    catch (Exception e) {
                        Toast.makeText(this, "Tag Refused To Format", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e) {
                    Toast.makeText(this, "Tag Refused To Connect", Toast.LENGTH_SHORT).show();
                }
                finally {
                    formatable.close();
                }
            }
            else {
                Toast.makeText(this, "Tag Does Not Support NDEF", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            // enable i/o
            Ndef.get(tag).connect();
            //write message
            ndef.writeNdefMessage(message);
            //close connection
            ndef.close();
        }
    }


    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        //set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        //copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload,1, langLength);
        System.arraycopy(textBytes,0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);

        return  recordNFC;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readfromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) && writePressed){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            cardIdText.setText("Card ID: "+bytesToHex(myTag.getId()));
           // dividerItemDecoration.setVisibility(View.VISIBLE);


            //check if an account already has this card id
            CollectionReference userss = fStore.collection("users");
            Query queryy =  userss.whereEqualTo("Card_Id", bytesToHex(myTag.getId()));
            queryy.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        //if no email, edit profile
                        if(task.getResult().isEmpty()){

                            //write to card and update database
                            try{
                                if(myTag == null){
                                    Toast.makeText(context, Error_Detected, Toast.LENGTH_LONG).show();
                                } else {
                                    NdefMessage message = new NdefMessage(new NdefRecord[] {

                                            NdefRecord.createTextRecord("en", mBalance2),
                                            NdefRecord.createTextRecord("en", mStudentId),
                                    });
                                    write(message,myTag);
                                    Toast.makeText(context, Write_Success, Toast.LENGTH_LONG).show();

                                    cardView.setVisibility(View.GONE);
                                    tapCardtoReadCardView.setVisibility(View.VISIBLE);


                                    //LOOK FOR user IN DATABASE AND update card id
                                    CollectionReference users = fStore.collection("users");
                                    Query query =  users.whereEqualTo("Student_ID", mStudentId);
                                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                //UPDATE RECIPIENT DATABASE ENTRY
                                                if(!task.getResult().isEmpty()){
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        DocumentReference documentReference = fStore.collection("users").document(document.getId());
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("Card_Id", bytesToHex(myTag.getId()));
                                                        documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                Toast.makeText(context, "DataBase Updated With New Card ID", Toast.LENGTH_SHORT).show();

                                                                Query query =  users.whereEqualTo("Student_ID", mStudentId);
                                                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mProgressBar2.setVisibility(View.INVISIBLE);

                                                                            myUsers.clear();

                                                                            //look for email in database
                                                                            if(!Objects.requireNonNull(task.getResult()).isEmpty()){
                                                                                int i = 0;
                                                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                    myUsers.add(i, document.toObject(myUser.class));
                                                                                    i++;
                                                                                }
                                                                                currentStudent.setText(myUsers.get(0).toString());

                                                                            }
                                                                            else {
                                                                                searchText.setError("Account With This Student ID Does Not Exist.");
                                                                            }
                                                                        } else {
                                                                            Log.d("TAG", "Error getting documents: ", task.getException());

                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                                Toast.makeText(context, "Failed To Update DataBase With New Card ID", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                                else {
                                                }
                                            } else {
                                                Log.d("TAG", "Error getting documents: ", task.getException());

                                            }
                                        }
                                    });
                                    writePressed = false;
                                }
                            }catch (IOException e){
                                Toast.makeText(context, Write_Error, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }catch (FormatException e){
                                Toast.makeText(context, "Format Error", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }
                        else {
                            Toast.makeText(context, "Account With This Card Id Already Exists.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());

                    }
                }
            });






        } else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()) && !writePressed){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfc_content.setVisibility(View.VISIBLE);

            listeningTextRead.setVisibility(View.INVISIBLE);
            tapCardGif.setVisibility(View.INVISIBLE);
            cardContentsText.setVisibility(View.VISIBLE);

            cardIdText.setText("Card ID: "+bytesToHex(myTag.getId()));
            cardIdText.setVisibility(View.VISIBLE);

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    protected void onResume() {
        super.onResume();
        WriteModeOn();
    }


    private void  WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writingTagFilters, null);
    }

    private void  WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }



    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(CheckSingleUser.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}