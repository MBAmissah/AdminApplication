package com.example.adminapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV2ApplicationKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.utils.Utilities;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import static com.nxp.nfclib.CardType.DESFireEV2;

public class CheckSingleUserTapLinx extends AppCompatActivity {

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

    //TAPLINX STUFF
    private String TAG = MainActivity.class.getSimpleName();
    private final String m_strLicense = "f7bf27540bbc196fb41e3123884422c6";
    private NxpNfcLib m_libInstance = null;

    public static final byte[] NEW_KEY_AES =
            {                                                    // New AES key
                    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0xff
            };

    public static final byte[] APP_KEY_AES =
            {                                                    // New AES key
                    (byte)0x1f, (byte)0x2f, (byte)0x3f, (byte)0x4f, (byte)0x5f, (byte)0x6f, (byte)0x7f,
                    (byte)0x8f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0xff
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_single_user_tap_linx);

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

        initializeLibrary();


    }

    private void initializeLibrary()
    {                                        // Register library with online license
        m_libInstance = NxpNfcLib.getInstance();
        m_libInstance.registerActivity( this, m_strLicense, "lV3q2OSj2Cj4ouIsZBre+FCwiZKZJIuxzBytDTItO9COm6L9rqcxKGuqfH6vPkiWo0ZBQ78YUMLZfER1OOG6FQ==" );
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


    @SuppressLint("SetTextI18n")
    @Override
    protected void onNewIntent(final Intent intent) {
        if(writePressed){
            if( DESFireEV2 == m_libInstance.getCardType( intent ) )
            {
                IDESFireEV2 objDESFireEV2 = DESFireFactory.getInstance()
                        .getDESFireEV2( m_libInstance.getCustomModules() );


                //check if an account already has this card id
                CollectionReference userss = fStore.collection("users");
                Query queryy =  userss.whereEqualTo("Card_Id", bytesToHex(objDESFireEV2.getUID()));
                queryy.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //if no email, edit profile
                            if(task.getResult().isEmpty()){

                                //write to card and update database
                                try{


                                    String data = mBalance2 + " " +  mStudentId;

                                    byte[] balanceBytes = mBalance2.getBytes();
                                    byte[] studentIdBytes = mStudentId.getBytes();
                                    byte[] dataBytes = data.getBytes();


                                    byte[] appId = new byte[]{0x12, 0x00, 0x00};
                                    int balanceFile = 0;
                                    int studentIdFile = 1;


                                    Key keyNew;
                                    KeyData keyDataNew;


                                    keyNew = new SecretKeySpec( NEW_KEY_AES, "AES" );
                                    keyDataNew = new KeyData();
                                    keyDataNew.setKey( keyNew );

                                    Key keyApp;
                                    KeyData keyDataApp;


                                    keyApp = new SecretKeySpec( APP_KEY_AES, "AES" );
                                    keyDataApp = new KeyData();
                                    keyDataApp.setKey( keyApp);


                                    objDESFireEV2.getReader().connect();
                                    objDESFireEV2.getReader().setTimeout(2000);

                                    //authenticate picc and select app
                                    objDESFireEV2.selectApplication(0);
                                    objDESFireEV2.authenticate( 0, IDESFireEV2.AuthType.AES, KeyType.AES128, keyDataNew);



                                    objDESFireEV2.selectApplication(appId);

                                    //authenticate app with application key 0
                                    objDESFireEV2.authenticate( 0, IDESFireEV2.AuthType.AES, KeyType.AES128, keyDataApp);


                                    // write to file
                                   // objDESFireEV2.writeData(balanceFile, 0, balanceBytes);
                                  //  objDESFireEV2.writeData(studentIdFile, 0, studentIdBytes);
                                    objDESFireEV2.writeData(0,0,dataBytes);



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
                                                        user.put("Card_Id", bytesToHex(objDESFireEV2.getUID()));
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

                                                                            //display found student
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
                                    cardView.setVisibility(View.GONE);
                                    tapCardtoReadCardView.setVisibility(View.VISIBLE);

                                    objDESFireEV2.getReader().close();

                                }
                                catch( Throwable t )
                                {
                                    t.printStackTrace();
                                    if(t.getMessage() == "Authentication Error"){
                                        Toast.makeText(CheckSingleUserTapLinx.this, "Please Format Card", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(t.getMessage() == "Tag was lost"){
                                        Toast.makeText(CheckSingleUserTapLinx.this, "Keep Card On Longer", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(CheckSingleUserTapLinx.this, "Please Try Again", Toast.LENGTH_SHORT).show();

                                    }
                                    writePressed = false;
                                    tapCardGif.setVisibility(View.INVISIBLE);
                                    cardView.setVisibility(View.GONE);
                                    objDESFireEV2.getReader().close();
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

                tapCardGif.setVisibility(View.INVISIBLE);
                writePressed = false;
            }
            else{
                Toast.makeText(this, "This card is not supported", Toast.LENGTH_SHORT).show();
            }



        }
        else {
            try {
                if (DESFireEV2 == m_libInstance.getCardType(intent)) {
                    IDESFireEV2 objDESFireEV2 = DESFireFactory.getInstance()
                            .getDESFireEV2(m_libInstance.getCustomModules());

                    //read card
                    byte[] appId = new byte[]{0x12, 0x00, 0x00};
                    int balanceFile = 0;
                    int studentIdFile = 1;


                    Key keyNew;
                    KeyData keyDataNew;
                    keyNew = new SecretKeySpec(NEW_KEY_AES, "AES");
                    keyDataNew = new KeyData();
                    keyDataNew.setKey(keyNew);


                    Key keyApp;
                    KeyData keyDataApp;
                    keyApp = new SecretKeySpec(APP_KEY_AES, "AES");
                    keyDataApp = new KeyData();
                    keyDataApp.setKey(keyApp);


                    objDESFireEV2.getReader().connect();
                    objDESFireEV2.getReader().setTimeout(2000);


                    //select and authenticate picc
                    objDESFireEV2.selectApplication(0);
                    objDESFireEV2.authenticate(0, IDESFireEV2.AuthType.AES, KeyType.AES128, keyDataNew);


                    //select and authenticate app
                    objDESFireEV2.selectApplication(appId);
                    //authenticate app with application key 0
                    objDESFireEV2.authenticate(0, IDESFireEV2.AuthType.AES, KeyType.AES128, keyDataApp);


                    // read
                    String data = new String(objDESFireEV2.readData(0, 0, 0));
                    String balance = data.split(" ")[0];
                    String id = data.split(" ")[1];

                    nfc_content.setText("Account Balance: GH₵ " + balance + "\nStudent ID: " + id);
                    nfc_content.setVisibility(View.VISIBLE);
                    listeningTextRead.setVisibility(View.INVISIBLE);
                    tapCardGif.setVisibility(View.INVISIBLE);
                    cardContentsText.setVisibility(View.VISIBLE);

                    cardIdText.setText("Card ID: " + bytesToHex(objDESFireEV2.getUID()));
                    cardIdText.setVisibility(View.VISIBLE);

                    objDESFireEV2.getReader().close();
                }

            else{
                Toast.makeText(this, "This card is not supported", Toast.LENGTH_SHORT).show();
            }
        }catch( Throwable t )
        {
            t.printStackTrace();
            if(t.getMessage() == "Authentication Error"){
                Toast.makeText(CheckSingleUserTapLinx.this, "Please Format Card", Toast.LENGTH_SHORT).show();
            }
            else if(t.getMessage() == "Tag was lost"){
                Toast.makeText(CheckSingleUserTapLinx.this, "Keep Card On Longer", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(CheckSingleUserTapLinx.this, "Please Try Again", Toast.LENGTH_SHORT).show();
            }
        }

        }



        super.onNewIntent(intent);


    }

    @Override
    protected void onResume()
    {                                        // Called if app becomes active
        Log.d( TAG, "onResume" );
        m_libInstance.startForeGroundDispatch();
        super.onResume();
    }

    @Override
    protected void onPause()
    {                                       // Called if app becomes inactive
        Log.d( TAG, "onPause" );
        m_libInstance.stopForeGroundDispatch();
        super.onPause();
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
        Intent setIntent = new Intent(CheckSingleUserTapLinx.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}