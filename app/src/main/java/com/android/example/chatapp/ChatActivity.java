package com.android.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.android.example.chatapp.adapters.PagerAdapter;
import com.android.example.chatapp.models.FirebaseModel;
import com.android.example.chatapp.profile.ProfileActivity;
import com.android.example.chatapp.specificChat.SpecificChat;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ChatActivity extends AppCompatActivity {

    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mFirebaseAuth;

    private CircularImageView mImageViewUser, mProfileView;

    FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder> mChatAdapter;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mRecyclerView = findViewById(R.id.recyclerView);

        Query query = mFirebaseFirestore.collection("Users").whereNotEqualTo("uid", mFirebaseAuth.getUid());
        FirestoreRecyclerOptions<FirebaseModel> allUsername = new FirestoreRecyclerOptions.
                Builder<FirebaseModel>().setQuery(query, FirebaseModel.class).build();

        mChatAdapter = new FirestoreRecyclerAdapter<FirebaseModel, NoteViewHolder>(allUsername) {

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(ChatActivity.this).inflate(R.layout.chat_view_layout, parent, false);
                return new NoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull FirebaseModel firebaseModel) {

                if(firebaseModel.getName() != null){
                    noteViewHolder.mUserNames.setText(firebaseModel.getName());
                }
                else{
                    Log.d("ChatsFragment", "Name null");
                }

                Glide.with(ChatActivity.this).load(firebaseModel.getImage()).into(mImageViewUser);

                if(firebaseModel.getStatus().equals("Online")){
                    noteViewHolder.mUserStatus.setText(firebaseModel.getStatus());
                    noteViewHolder.mUserStatus.setTextColor(Color.GREEN);
                }
                else {
                    noteViewHolder.mUserStatus.setText(firebaseModel.getStatus());
                    noteViewHolder.mUserStatus.setTextColor(Color.parseColor("#C6C6C6"));
                }

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, SpecificChat.class);
                        intent.putExtra("name", firebaseModel.getName());
                        intent.putExtra("phoneNo", firebaseModel.getPhoneNo());
                        intent.putExtra("receiverUid", firebaseModel.getUid());
                        intent.putExtra("imageUri", firebaseModel.getImage());
                        startActivity(intent);
                    }
                });
            }
        };

        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(mRecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);

        mProfileView = findViewById(R.id.profileIv);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        storageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String token = uri.toString();
                Glide.with(ChatActivity.this).load(token).into(mProfileView);
            }
        });

        mProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView mUserNames, mUserStatus;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserNames = itemView.findViewById(R.id.user_name);
            mUserStatus = itemView.findViewById(R.id.user_status);
            mImageViewUser = itemView.findViewById(R.id.iv_of_user);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        storageReference.child("images").child(mFirebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String token = uri.toString();
                Glide.with(ChatActivity.this).load(token).into(mProfileView);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference = mFirebaseFirestore.collection("Users").document(mFirebaseAuth.getUid());
        documentReference.update("status", "Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
//                Toast.makeText(getApplicationContext(), "User is Offline", Toast.LENGTH_SHORT).show();
            }
        });
        Log.e("ChatActivity -> Offline", mFirebaseAuth.getUid());
        if(mChatAdapter != null){
            mChatAdapter.stopListening();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference = mFirebaseFirestore.collection("Users").document(mFirebaseAuth.getUid());
        documentReference.update("status", "Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
//                Toast.makeText(getApplicationContext(), "User is Online", Toast.LENGTH_SHORT).show();
            }
        });
        Log.e("ChatActivity -> Online", mFirebaseAuth.getUid());
        mChatAdapter.startListening();
    }
}