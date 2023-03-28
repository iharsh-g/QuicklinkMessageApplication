package com.android.example.chatapp.userverification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeUnit;

public class MobileLoginActivity extends AppCompatActivity {

    private EditText mGetPhoneNumber;
    private CountryCodePicker mCountryCodePicker;
    private String countryCode, codeSent, phoneNumber;

    private FirebaseAuth mFirebaseAuth;
    private ProgressBar mProgressBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        mCountryCodePicker = findViewById(R.id.country_code_picker);
        mGetPhoneNumber = findViewById(R.id.et_get_phone_number);
        CardView mSendOtp = findViewById(R.id.send_otp);
        mProgressBar = findViewById(R.id.pb_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();

        mCountryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode = mCountryCodePicker.getSelectedCountryCodeWithPlus();
            }
        });

        mSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                number = mGetPhoneNumber.getText().toString();
                if (number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Number", Toast.LENGTH_SHORT).show();
                } else if (number.length() < 10) {
                    Toast.makeText(getApplicationContext(), "Please Enter Correct Number", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    phoneNumber = countryCode + number;

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mFirebaseAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MobileLoginActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                //Automatic fetch Code
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, PhoneAuthProvider.@NonNull ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(), "OTP is Sent", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
                codeSent = s;
                Intent intent = new Intent(MobileLoginActivity.this, OtpAuthenticationActivity.class);
                intent.putExtra("otp", codeSent);
                startActivity(intent);
            }
        };
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mFirebaseAuth.getInstance().getCurrentUser() != null) {
//            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
//    }
}