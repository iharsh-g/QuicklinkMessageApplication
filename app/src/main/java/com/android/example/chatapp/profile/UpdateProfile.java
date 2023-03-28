package com.android.example.chatapp.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.example.chatapp.ChatActivity;
import com.android.example.chatapp.R;
import com.android.example.chatapp.models.UserProfile;
import com.android.example.chatapp.userverification.SetProfileActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class UpdateProfile extends AppCompatActivity {

    private Uri imagePath;
    private String imageUriAccessToken, newName;

    private EditText mNewUserName;
    private CircularImageView mGetNewImageInImageView;
    private ProgressBar mProgressbar;

    private static final int PICK_IMAGE = 123;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseFirestore mFirebaseFirestore;
    private StorageReference mStorageReference;
    private FirebaseStorage mFirebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        androidx.appcompat.widget.Toolbar mToolbar = findViewById(R.id.toolbar_update_profile);
        ImageButton mBackButton = findViewById(R.id.back_button_update_profile);
        mGetNewImageInImageView = findViewById(R.id.get_new_user_image_in_iv);
        mProgressbar = findViewById(R.id.pb_update_profile);
        mNewUserName = findViewById(R.id.get_new_user_name);
        CardView mUpdateProfileButton = findViewById(R.id.update_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        setSupportActionBar(mToolbar);
        mBackButton.setOnClickListener(v -> finish());

        mNewUserName.setText(intent.getStringExtra("username"));
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(mFirebaseAuth.getUid());
        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = mNewUserName.getText().toString();
                if(newName.isEmpty()){
                    Toast.makeText(UpdateProfile.this, "Name is Empty", Toast.LENGTH_SHORT).show();
                }
                else if(imagePath != null){
                    UserProfile userProfile;
                    if(mFirebaseAuth.getCurrentUser().getPhoneNumber() != null)
                        userProfile = new UserProfile(newName, mFirebaseAuth.getUid(), mFirebaseAuth.getCurrentUser().getPhoneNumber(), "");
                    else
                        userProfile = new UserProfile(newName , mFirebaseAuth.getUid(), "", mFirebaseAuth.getCurrentUser().getEmail());

                    databaseReference.child("users").child(mFirebaseAuth.getCurrentUser().getUid()).setValue(userProfile);

                    updateImageToStorage();
                    
                    Toast.makeText(UpdateProfile.this, "Updated", Toast.LENGTH_SHORT).show();
                    mProgressbar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    UserProfile userProfile;
                    if(mFirebaseAuth.getCurrentUser().getPhoneNumber() != null)
                        userProfile = new UserProfile(newName, mFirebaseAuth.getUid(), mFirebaseAuth.getCurrentUser().getPhoneNumber(), "");
                    else
                        userProfile = new UserProfile(newName, mFirebaseAuth.getUid(), "", mFirebaseAuth.getCurrentUser().getEmail());

                    databaseReference.child("users").
                            child(mFirebaseAuth.getCurrentUser().getUid())
                            .setValue(userProfile);
                    
                    updateNameToCloudFirestore();

                    Toast.makeText(UpdateProfile.this, "Update", Toast.LENGTH_SHORT).show();
                    mProgressbar.setVisibility(View.INVISIBLE);
                    finish();
                }
            }
        });

        mGetNewImageInImageView.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent1, PICK_IMAGE);
        });

        mStorageReference = mFirebaseStorage.getReference();
        mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Glide.with(UpdateProfile.this).load(imageUriAccessToken).into(mGetNewImageInImageView);
            }
        });

    }

    private void updateImageToStorage() {
        StorageReference imageRef = mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic");

        //Image compression

        Bitmap bitmap=null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        ///putting image to storage

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(UpdateProfile.this,"URI get success",Toast.LENGTH_SHORT).show();
                        updateNameToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfile.this,"URI get Failed",Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(UpdateProfile.this,"Image is Updated",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Image Not Updated",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNameToCloudFirestore() {
        DocumentReference documentReference = mFirebaseFirestore.collection("Users").document(mFirebaseAuth.getUid());
        Map<String , Object> userdata=new HashMap<>();
        userdata.put("name", newName);
        userdata.put("image", imageUriAccessToken);
        userdata.put("uid", mFirebaseAuth.getUid());
        userdata.put("status", "Online");


        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(UpdateProfile.this,"Profile Update Successfully",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imagePath = data.getData();
            mGetNewImageInImageView.setImageURI(imagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}