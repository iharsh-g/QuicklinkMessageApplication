package com.android.example.chatapp.userverification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.android.example.chatapp.ChatActivity;
import com.android.example.chatapp.R;
import com.android.example.chatapp.databinding.ActivitySetProfileBinding;
import com.android.example.chatapp.models.UserProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class SetProfileActivity extends AppCompatActivity {

    private static int PICK_IMAGE = 123;
    private Uri imagePath;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private String name, imageUriAccessToken;

    private boolean openGallery = false, existingUser = false;

    private ActivitySetProfileBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_set_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        //If User already Exists
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(mFirebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserProfile mUserProfile = snapshot.getValue(UserProfile.class);
                if(mUserProfile != null) {
                    existingUser = true;
                    getDataFromFirebaseFirestore();
                    mBinding.proceedProfile.setVisibility(View.VISIBLE);
                    mBinding.saveProfile.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        imagePath = Uri.parse("android.resource://com.android.example.chatapp/drawable/defaultprofile");
        mBinding.ivGetUserImageInIv.setImageURI(imagePath);

        mBinding.ivGetUserImageInIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        //If user already exists the save profile button will invisible and proceed button will visible
        mBinding.proceedProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mBinding.etGetUserName.getText().toString().trim();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    mBinding.pbSetProfile.setVisibility(View.VISIBLE);
                    sendDataToRealTimeDatabase();
                    mBinding.pbSetProfile.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(SetProfileActivity.this, ChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });

        mBinding.saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = mBinding.etGetUserName.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    mBinding.pbSetProfile.setVisibility(View.VISIBLE);
                    sendDataToRealTimeDatabase();
                    mBinding.pbSetProfile.setVisibility(View.INVISIBLE);

                    Intent intent = new Intent(SetProfileActivity.this, ChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    //If User already exists
    private void getDataFromFirebaseFirestore() {
        //Getting profile photo
        mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Glide.with(SetProfileActivity.this).load(imageUriAccessToken).into(mBinding.ivGetUserImageInIv);
            }
        });

        //Getting User Name
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(mFirebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile mUserProfile = snapshot.getValue(UserProfile.class);
                mBinding.etGetUserName.setText(mUserProfile.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to fetch", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            openGallery = true;
            imagePath = data.getData();
            mBinding.ivGetUserImageInIv.setImageURI(imagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDataToRealTimeDatabase() {
        name = mBinding.etGetUserName.getText().toString().trim();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //If user already exists it changes its name otherwise it creates
        UserProfile userProfile;
        if(mFirebaseAuth.getCurrentUser().getPhoneNumber() != null)
             userProfile = new UserProfile(name, mFirebaseAuth.getUid(), mFirebaseAuth.getCurrentUser().getPhoneNumber(), "");
        else
            userProfile = new UserProfile(name, mFirebaseAuth.getUid(), "", mFirebaseAuth.getCurrentUser().getEmail());

        firebaseDatabase.getReference().child("users").
                child(mFirebaseAuth.getCurrentUser().getUid())
                .setValue(userProfile);

        Toast.makeText(getApplicationContext(), "User Profile Added Successfully", Toast.LENGTH_SHORT).show();

        sendImageToStorage();
    }

    private void sendImageToStorage() {
        /* If a user is existing then it means the image already saved in database but 2 cases arrive
             1 -> if user not open gallery -> We already have imageUriAccessToken from getDataFromFirestore() Fn. we will simply update it
             2 -> if user open gallery -> then will have image path from onActivityResult and we need token
        */

        /*
            If user is new the 2 cases
             *3 -> if not open gallery -> then imagePath already set above in onCreate function and we need token
              4 -> if open gallery -> then imagePath will be set in the onActivityResult and we need token
         */

        if(existingUser && !openGallery) {
            sendDataToCloudFirestore();
        }

        StorageReference imageRef = mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic");

        Bitmap bitmap = null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e){
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "Uri get Success", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Uri get Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Image is Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image not Uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataToCloudFirestore() {
        DocumentReference documentReference = mFirebaseFirestore.collection("Users").document(mFirebaseAuth.getUid());
        Map<String , Object> userdata = new HashMap<>();
        userdata.put("name", name);
        userdata.put("image", imageUriAccessToken);
        userdata.put("uid", mFirebaseAuth.getUid());

        if(mFirebaseAuth.getCurrentUser().getPhoneNumber() != null) {
            userdata.put("phoneNo", mFirebaseAuth.getCurrentUser().getPhoneNumber());
            userdata.put("email", "");
        }
        else {
            userdata.put("phoneNo", "");
            userdata.put("email", mFirebaseAuth.getCurrentUser().getEmail());
        }

        userdata.put("status", "Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data on CloudFirestore Success", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
