package com.android.example.chatapp.specificChat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.android.example.chatapp.adapters.MessagesAdapter;
import com.android.example.chatapp.databinding.ActivitySpecificChatBinding;
import com.android.example.chatapp.models.Messages;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SpecificChat extends AppCompatActivity {

    HashMap<String, String> mMap = new HashMap<>();
    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private String enteredMessage, mReceiverName, mReceiverUid, mSenderUid, currentTime;
    private String senderRoom,receiverRoom;
    private ActivitySpecificChatBinding mBinding;

    private Intent intent;

    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    private MessagesAdapter mMessagesAdapter;
    private ArrayList<Messages> mMessagesArrayList;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;

    private android.os.Handler customHandler;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_specific_chat);

        mMessagesArrayList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mBinding.recyclerViewSpecificChat.setLayoutManager(linearLayoutManager);

        mMessagesAdapter = new MessagesAdapter(this, mMessagesArrayList, this);
        mBinding.recyclerViewSpecificChat.setAdapter(mMessagesAdapter);

        intent = getIntent();
        setSupportActionBar(mBinding.toolbarSpecificChat);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm:a");

        mSenderUid = mFirebaseAuth.getUid();
        mReceiverUid = getIntent().getStringExtra("receiverUid");
        mReceiverName = getIntent().getStringExtra("name");

        senderRoom = mSenderUid + mReceiverUid;
        receiverRoom = mReceiverUid + mSenderUid;

        // When there is change in state of edittext input
        mBinding.getMessage.addTextChangedListener(new TextWatcher() {
            // when there is no text added
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() == 0) {
                    // set text to Not typing
                    mBinding.cvCamera.setVisibility(View.VISIBLE);
                    mBinding.cvMic.setVisibility(View.VISIBLE);
                    mBinding.cvSendMessage.setVisibility(View.GONE);

                    setEditTextToCameraCv();
                } else {
                    // set text to typing
                    mBinding.cvCamera.setVisibility(View.GONE);
                    mBinding.cvMic.setVisibility(View.GONE);
                    mBinding.cvSendMessage.setVisibility(View.VISIBLE);

                    setEditTextToMessageCv();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.cvCamera.setVisibility(View.GONE);
                mBinding.cvMic.setVisibility(View.GONE);
                mBinding.cvSendMessage.setVisibility(View.VISIBLE);

                setEditTextToMessageCv();
            }

            // after we input some text
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    // set text to Stopped typing
                    mBinding.cvCamera.setVisibility(View.VISIBLE);
                    mBinding.cvMic.setVisibility(View.VISIBLE);
                    mBinding.cvSendMessage.setVisibility(View.GONE);

                    setEditTextToCameraCv();
                }
            }
        });

        //Phone Call
        mBinding.ivCall.setOnClickListener(v -> {

            if(intent.getStringExtra("phoneNo").equals("")) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Can't Make Call");
                dialog.setMessage("User does not signed in with mobile number");
                dialog.setPositiveButton("Ok", (dialog1, which) -> {
                    dialog1.dismiss();
                });
                dialog.show();
                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + intent.getStringExtra("phoneNo")));
            if (ContextCompat.checkSelfPermission(SpecificChat.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(SpecificChat.this, new String[]{Manifest.permission.CALL_PHONE},
                        100);
                //onRequestPermissionsResult will called
            }
            else {
                //You already have permission
                try {
                    startActivity(callIntent);
                }
                catch(SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        //Retrieving Chats
        DatabaseReference databaseReference = mFirebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
        mMessagesAdapter = new MessagesAdapter(SpecificChat.this, mMessagesArrayList, this);
        databaseReference.addValueEventListener(new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessagesArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()) {
                    Messages messages = snapshot1.getValue(Messages.class);
                    mMessagesArrayList.add(messages);
                }
                mMessagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mBinding.backButtonSpecificChat.setOnClickListener(v -> finish());

        //---------------------------------  Getting Name, Status --------------------------------//
        mBinding.nameSpecificUser.setText(mReceiverName);
        mBinding.nameSpecificUser.setSelected(true);
        String uri = intent.getStringExtra("imageUri");
        if(uri.isEmpty()){
            Toast.makeText(SpecificChat.this, "null is received", Toast.LENGTH_SHORT).show();
        } else {
            Glide.with(this).load(uri).into(mBinding.specificUserImageInImageview);
        }

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference1 = firebaseFirestore.collection("Users").document(mSenderUid);
        documentReference1.update("status", "Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {  }
        });
        DocumentReference documentReference2 = firebaseFirestore.collection("Users").document(mReceiverUid);
        documentReference2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String status = documentSnapshot.getString("status");
                if(status.equals("Offline")) {
                    mBinding.statusSpecificUser.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBinding.nameSpecificUser.getLayoutParams();
                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                    mBinding.nameSpecificUser.setLayoutParams(layoutParams);
                }
                else {
                    mBinding.statusSpecificUser.setVisibility(View.VISIBLE);
                    mBinding.statusSpecificUser.setText(status);
                }
            }
        });

        mBinding.mic.setOnClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            speak();
        });

        // Gallery
        mBinding.ivGallery.setOnClickListener(v -> {
            String senderToken, receiverToken;
            if(!mMap.containsKey(mSenderUid)) {
                senderToken = getRandomString(16);
            }
            else {
                senderToken = mMap.get(mSenderUid);
            }

            if(!mMap.containsKey(mReceiverUid)) {
                receiverToken = getRandomString(16);
            }
            else {
                receiverToken = mMap.get(mReceiverUid);
            }

            String token = senderToken + receiverToken;

            Intent intent = new Intent(SpecificChat.this, GalleryActivity.class);
            intent.putExtra("name", mReceiverName);
            intent.putExtra("receiverUid", mReceiverUid);
            intent.putExtra("token", token);
            intent.putExtra("imageUri", uri);
            startActivity(intent);
        });

        //Camera
        mBinding.cvCamera.setOnClickListener(v -> {
            String senderToken, receiverToken;
            if(!mMap.containsKey(mSenderUid)) {
                senderToken = getRandomString(16);
            }
            else {
                senderToken = mMap.get(mSenderUid);
            }

            if(!mMap.containsKey(mReceiverUid)) {
                receiverToken = getRandomString(16);
            }
            else {
                receiverToken = mMap.get(mReceiverUid);
            }

            String token = senderToken + receiverToken;

            Intent intent = new Intent(SpecificChat.this, CameraActivity.class);
            intent.putExtra("name", mReceiverName);
            intent.putExtra("receiverUid", mReceiverUid);
            intent.putExtra("token", token);
            intent.putExtra("imageUri", uri);
            startActivity(intent);
        });

        //Message Button
        mBinding.cvSendMessage.setOnClickListener(v -> {
            enteredMessage = mBinding.getMessage.getText().toString();
            if(enteredMessage.isEmpty()){
                Toast.makeText(SpecificChat.this, "Enter Message First", Toast.LENGTH_SHORT).show();
            }
            else {
                String senderToken, receiverToken;
                if(!mMap.containsKey(mSenderUid)) {
                    senderToken = getRandomString(16);
                }
                else {
                    senderToken = mMap.get(mSenderUid);
                }

                if(!mMap.containsKey(mReceiverUid)) {
                    receiverToken = getRandomString(16);
                }
                else {
                    receiverToken = mMap.get(mReceiverUid);
                }


                String token = senderToken + receiverToken;
                String encryptMsg = Encrypt(enteredMessage, token);

                Date date = new Date();
                currentTime = simpleDateFormat.format(calendar.getTime());
                Messages messages = new Messages(encryptMsg, mFirebaseAuth.getUid(), date.getTime(), currentTime, token, false);
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mFirebaseDatabase.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mFirebaseDatabase.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .push()
                                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Successfully Send
                            }
                        });
                    }
                });
                mBinding.getMessage.setText(null);
            }
        });

        //Repeatedly set for checking online offline
        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    private void setEditTextToMessageCv() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBinding.getMessage.getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, R.id.cv_send_message);
        Log.e("EditText", "" + mBinding.cvSendMessage.getId());
        mBinding.getMessage.setLayoutParams(layoutParams);
    }

    private void setEditTextToCameraCv() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBinding.getMessage.getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, R.id.cv_camera);
        Log.e("EditText", "" + mBinding.cvMic.getId());
        mBinding.getMessage.setLayoutParams(layoutParams);
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Something...");

        try {
            startActivityForResult(intent, 100);
        }
        catch (Exception e) {
            Toast.makeText(SpecificChat.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable updateTimerThread = new Runnable()
    {
        public void run()
        {
            //write here whatever you want to repeat
            customHandler.postDelayed(this, 10000);
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(mReceiverUid);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String status = documentSnapshot.getString("status");
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBinding.nameSpecificUser.getLayoutParams();
                    if(status.equals("Offline")) {
                        mBinding.statusSpecificUser.setVisibility(View.GONE);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        mBinding.nameSpecificUser.setLayoutParams(layoutParams);
                    }
                    else {
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                        mBinding.nameSpecificUser.setLayoutParams(layoutParams);

                        mBinding.statusSpecificUser.setVisibility(View.VISIBLE);
                        mBinding.statusSpecificUser.setText(status);
                    }
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if(resultCode == RESULT_OK && data != null) {
                    ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mBinding.getMessage.setText(res.get(0));

                    mBinding.cvCamera.setVisibility(View.GONE);
                    mBinding.cvMic.setVisibility(View.GONE);
                    mBinding.cvSendMessage.setVisibility(View.VISIBLE);

                    setEditTextToMessageCv();
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the phone call

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + intent.getStringExtra("phoneNo")));
                    startActivity(callIntent);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(SpecificChat.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mMessagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mMessagesAdapter != null){
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseReference databaseReference = mFirebaseDatabase.getReference().child("chats").child(senderRoom).child("messages");
        mMessagesAdapter = new MessagesAdapter(SpecificChat.this, mMessagesArrayList, this);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMessagesArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()) {
                    Messages messages=snapshot1.getValue(Messages.class);
                    mMessagesArrayList.add(messages);
                }
                mMessagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static String Encrypt(String value, String token){
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(token.getBytes("utf-8"), "AES"), new IvParameterSpec(iv));

            byte[] cipherText = cipher.doFinal(value.getBytes("utf-8"));
            byte[] ivAndCipherText = getCombinedArray(iv, cipherText);

            return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] getCombinedArray(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];
        for(int i = 0; i < combined.length; i++){
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}