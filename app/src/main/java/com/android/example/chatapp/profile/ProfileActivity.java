package com.android.example.chatapp.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.android.example.chatapp.models.UserProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ProfileActivity extends AppCompatActivity {

    private TextView mViewUserName;
    private String imageUriAccessToken;
    private CircularImageView mViewUserImageInImageView;

    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mViewUserImageInImageView = findViewById(R.id.iv_view_user_image_in_iv);
        mViewUserName = findViewById(R.id.view_user_name);
        CardView mUpdateProfile = findViewById(R.id.update_profile);
        androidx.appcompat.widget.Toolbar mToolbar = findViewById(R.id.toolbar_profile);
        ImageButton mBackButton = findViewById(R.id.back_button_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();

        setSupportActionBar(mToolbar);
        mBackButton.setOnClickListener(v -> finish());

        //image
        mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Glide.with(ProfileActivity.this).load(imageUriAccessToken).into(mViewUserImageInImageView);
            }
        });

        //realtime database data fetching
        TextView tv_phone = findViewById(R.id.view_user_phone_no);
        TextView tv_email = findViewById(R.id.view_user_id);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(mFirebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile mUserProfile = snapshot.getValue(UserProfile.class);
                mViewUserName.setText(mUserProfile.getUserName());

                if(mUserProfile.getEmailId().equals("")) {
                    tv_email.setVisibility(View.GONE);
                    tv_phone.setVisibility(View.VISIBLE);

                    tv_phone.setText(mUserProfile.getPhoneNum());
                }
                else {
                    tv_email.setVisibility(View.VISIBLE);
                    tv_phone.setVisibility(View.GONE);

                    tv_email.setText(mUserProfile.getEmailId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to fetch", Toast.LENGTH_SHORT).show();
            }
        });

        mUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfile.class);
            intent.putExtra("username", mViewUserName.getText().toString());
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mStorageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageUriAccessToken = uri.toString();
                Glide.with(ProfileActivity.this).load(imageUriAccessToken).into(mViewUserImageInImageView);
            }
        });
    }
}