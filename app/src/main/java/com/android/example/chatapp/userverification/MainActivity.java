package com.android.example.chatapp.userverification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.example.chatapp.ChatActivity;
import com.android.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie);
        lottieAnimationView.animate();
        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);

        CardView cv1 = findViewById(R.id.number);
        cv1.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, MobileLoginActivity.class));
        });

        CardView cv2 = findViewById(R.id.email);
        cv2.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mFirebaseAuth.getInstance().getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}