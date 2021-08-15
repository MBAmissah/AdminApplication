package com.example.adminapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.utils.Utilities;


import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import static com.nxp.nfclib.CardType.DESFireEV2;

public class MainActivity2 extends AppCompatActivity {

    ImageView tapCardGif;
    Button systemInitialize, formatCard;
    boolean initPressed, formatPressed;

    private String TAG = MainActivity.class.getSimpleName();
    private final String m_strLicense = "f7bf27540bbc196fb41e3123884422c6";
    private NxpNfcLib m_libInstance = null;

    public static final byte[] READ_KEY_AES_1 =
            {                                                    // AES key for file #1
                    (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
                    (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x01,
                    (byte)0x01, (byte)0x01
            };

    public static final byte[] READ_KEY_AES_2 =
            {                                                    // AES key for file #2
                    (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02,
                    (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02, (byte)0x02,
                    (byte)0x02, (byte)0x02
            };

    public static final byte[] DEFAULT_KEY_AES =
            {                                                    // Default AES key for MIFARE DESFire
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00
            };

    public static final byte[] DEFAULT_VALUE_BLOCK =
            {                                                    // Default value block of value 1
                    (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfe, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x5a, (byte)0xa5,
                    (byte)0x5a, (byte)0xa5
            };

    public static final byte[] DEFAULT_MIFARE_KEY =
            {                                                  // Default key for MIFARE Classic
                    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
            };


    public static final byte[] DEFAULT_KEY_2K3DES =
            {                                                    // Default key for MIFARE DESFire
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00
            };

    public static final byte[] NEW_KEY_AES =
            {                                                    // New AES key
                    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
                    (byte)0xff, (byte)0xff
            };

    public static final byte[] DEFAULT_KEY_ULTRALIGHT_C =
            {                                                  // Default key for MIFARE Ultralight C
                    (byte)0x49, (byte)0x45, (byte)0x4D, (byte)0x4B, (byte)0x41, (byte)0x45, (byte)0x52,
                    (byte)0x42, (byte)0x21, (byte)0x4E, (byte)0x41, (byte)0x43, (byte)0x55, (byte)0x4F,
                    (byte)0x59, (byte)0x46
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        systemInitialize = findViewById(R.id.systeminitialize);
        formatCard = findViewById(R.id.formatcard);
        tapCardGif = findViewById(R.id.listening2);

        systemInitialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPressed = true;
                formatPressed = false;
                tapCardGif.setVisibility(View.VISIBLE);
            }
        });

        formatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formatPressed = true;
                initPressed = false;
                tapCardGif.setVisibility(View.VISIBLE);
            }
        });

        initializeLibrary();
    }

    private void initializeLibrary()
    {                                        // Register library with online license
        m_libInstance = NxpNfcLib.getInstance();
        m_libInstance.registerActivity( this, m_strLicense, "lV3q2OSj2Cj4ouIsZBre+FCwiZKZJIuxzBytDTItO9COm6L9rqcxKGuqfH6vPkiWo0ZBQ78YUMLZfER1OOG6FQ==" );
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

    @Override
    public void onNewIntent( final Intent intent )
    {
        Log.d( TAG, "onNewIntent" );

        if(initPressed){
            initCardLogic( intent );
        }

        if(formatPressed){
            formatCardLogic( intent );
        }

       // cardLogic( intent );
        super.onNewIntent( intent );
    }


   private void initCardLogic( final Intent intent )
   {
      Key keyDefault;
      KeyData keyDataDefault;

      if( DESFireEV2 == m_libInstance.getCardType( intent ) )
      {
         IDESFireEV2 objDESFireEV2 = DESFireFactory.getInstance()
                                                 .getDESFireEV2( m_libInstance.getCustomModules() );
         try
         {
            objDESFireEV2.getReader().connect();
                                          //Read key for file #1

            keyDefault = new SecretKeySpec( DEFAULT_KEY_2K3DES, "DESede" );
            keyDataDefault = new KeyData();
            keyDataDefault.setKey( keyDefault );

            // Address PICC Master Key
            objDESFireEV2.selectApplication( 0 );
            //Authenticate
            objDESFireEV2.authenticate( 0, IDESFireEV2.AuthType.Native, KeyType.THREEDES, keyDataDefault );


            //Change PICC Master Key
             objDESFireEV2.changeKey(0,KeyType.AES128, DEFAULT_KEY_2K3DES,NEW_KEY_AES,(byte)0);
             Toast.makeText(this, "Card Initialized Successfully", Toast.LENGTH_SHORT).show();

             tapCardGif.setVisibility(View.INVISIBLE);
             initPressed = false;

         }
         catch( Throwable t )
         {
            t.printStackTrace();
            if(t.getMessage() == "Authentication Error"){
                Toast.makeText(this, "Please Format Card", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
            }
             tapCardGif.setVisibility(View.INVISIBLE);
             initPressed = false;
         }
      }
      else{
          Toast.makeText(this, "This card is not supported", Toast.LENGTH_SHORT).show();
      }
   }

    private void formatCardLogic( final Intent intent )
    {
        Key keyDefault;
        KeyData keyDataDefault;

        if( DESFireEV2 == m_libInstance.getCardType( intent ) )
        {
            IDESFireEV2 objDESFireEV2 = DESFireFactory.getInstance()
                    .getDESFireEV2( m_libInstance.getCustomModules() );
            try
            {
                objDESFireEV2.getReader().connect();

                //Read key for file #1
                keyDefault = new SecretKeySpec( NEW_KEY_AES, "AES" );
                keyDataDefault = new KeyData();
                keyDataDefault.setKey( keyDefault );


                // Address PICC Master Key
                objDESFireEV2.selectApplication( 0 );
                //Authenticate
                objDESFireEV2.authenticate( 0, IDESFireEV2.AuthType.AES, KeyType.AES128, keyDataDefault);


                //Format
                objDESFireEV2.format();

                //Change PICC Master Key
                objDESFireEV2.changeKey(0,KeyType.THREEDES, NEW_KEY_AES,DEFAULT_KEY_2K3DES,(byte)0);


                Toast.makeText(this, "Card Formatted Successfully", Toast.LENGTH_SHORT).show();

                tapCardGif.setVisibility(View.INVISIBLE);
                initPressed = false;
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            if(t.getMessage() == "Authentication Error"){
                Toast.makeText(this, "Card Not Supported", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Please Try Again", Toast.LENGTH_SHORT).show();
            }
                tapCardGif.setVisibility(View.INVISIBLE);
                initPressed = false;
            }
        }
        else{
            Toast.makeText(this, "This card is not supported", Toast.LENGTH_SHORT).show();
        }
    }
}