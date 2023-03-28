package com.android.example.chatapp.userverification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.checkerframework.checker.nullness.qual.NonNull;

public class OtpAuthenticationActivity extends AppCompatActivity {

    private EditText mGetOtp;
    private String enterOtp;

    private FirebaseAuth mFirebaseAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);

        mGetOtp = findViewById(R.id.et_get_otp);
        TextView mChangeNumber = findViewById(R.id.tv_change_number);
        CardView mVerifyOtp = findViewById(R.id.verify_otp);
        mProgressBar = findViewById(R.id.pb_opt_auth);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mChangeNumber.setOnClickListener(v -> {
            Intent intent = new Intent(OtpAuthenticationActivity.this, MainActivity.class);
            startActivity(intent);
        });

        mVerifyOtp.setOnClickListener(v -> {
            enterOtp = mGetOtp.getText().toString();
            if(enterOtp.isEmpty()){
                Toast.makeText(getApplicationContext(), "Enter OTP", Toast.LENGTH_SHORT).show();
            }
            else{
                mProgressBar.setVisibility(View.VISIBLE);
                String codeReceived = getIntent().getStringExtra("otp");
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeReceived, enterOtp);
                signInWithPhoneAuthCredentials(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OtpAuthenticationActivity.this, SetProfileActivity.class);
                    startActivity(intent);
                } else {
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Check your OTP", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}