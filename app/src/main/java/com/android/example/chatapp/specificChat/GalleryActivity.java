package com.android.example.chatapp.specificChat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.android.example.chatapp.databinding.ActivityGalleryBinding;
import com.android.example.chatapp.models.Messages;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GalleryActivity extends AppCompatActivity {

    private ActivityGalleryBinding mBinding;
    private String mReceiverUid, mSenderUid, senderRoom, receiverRoom, imageString, token;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_gallery);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        token = getIntent().getStringExtra("token");
        mReceiverUid = getIntent().getStringExtra("receiverUid");
        mBinding.nameSpecificUser.setText(getIntent().getStringExtra("name"));
        String uri = getIntent().getStringExtra("imageUri");

        if(uri.isEmpty()){
            Toast.makeText(GalleryActivity.this, "null is received", Toast.LENGTH_SHORT).show();
        } else {
            Glide.with(this).load(uri).into(mBinding.specificUserImageInImageview);
        }

        mSenderUid = mFirebaseAuth.getUid();
        senderRoom = mSenderUid + mReceiverUid;
        receiverRoom = mReceiverUid + mSenderUid;

        mBinding.backButtonSpecificChat.setOnClickListener(v -> finish());

        mBinding.ivSelected.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Title"), 1);
        });

        mBinding.cvSendMessage.setOnClickListener(v -> {
            if(imageString == null || imageString == "") {
                Toast.makeText(GalleryActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
            }
            else {
                String encryptedString = Encrypt(imageString, token);

                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:a");
                String currentTime = simpleDateFormat.format(calendar.getTime());

                Messages messages = new Messages(encryptedString, mFirebaseAuth.getUid(), date.getTime(), currentTime, token, true);
                mFirebaseDatabase.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        mFirebaseDatabase.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .push()
                                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                //Successfully Send
                                Toast.makeText(GalleryActivity.this, "Image Send Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0)
            finish();
        else if(requestCode == 1) {
            mBinding.tvSelect.setVisibility(View.GONE);
            Uri uri = data.getData();
            Log.e("MainActivity", uri.toString());
            mBinding.ivSelected.setImageURI(uri);

            Bitmap bitmap = null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
            byte[] dataes = byteArrayOutputStream.toByteArray();

            imageString = Base64.encodeToString(dataes, Base64.NO_WRAP);
            Log.e("ImageString", imageString);
        }
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
}