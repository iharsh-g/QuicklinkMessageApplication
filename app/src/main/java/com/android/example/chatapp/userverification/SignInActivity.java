package com.android.example.chatapp.userverification;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SignInActivity extends AppCompatActivity {

    private EditText mEmail, mPass;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmail = findViewById(R.id.et_signIn_email);
        mPass = findViewById(R.id.et_signIn_password);

        mFirebaseAuth = FirebaseAuth.getInstance();

        TextView tv = findViewById(R.id.tv_register);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, RegistrationActivity.class));
            }
        });

        CardView bt = findViewById(R.id.signIn);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = mEmail.getText().toString();
                String Pass = mPass.getText().toString();

                if (Email.equals("") || Pass.equals("")) {
                    makeErrorDialog();
                    return;
                }

                makeDialog(Email, Pass);
            }
        });

        TextView textViewForgotPass = findViewById(R.id.tv_forgot_pass);
        textViewForgotPass.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SignInActivity.this, R.style.BottomSheetTheme);
            View sheetView = LayoutInflater.from(SignInActivity.this).inflate(R.layout.bottom_sheet_layout, null);

            EditText editTextEmail = sheetView.findViewById(R.id.et_email);
            CardView send = sheetView.findViewById(R.id.send);

            send.setOnClickListener(v -> {
                String email = editTextEmail.getText().toString();
                mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.cancel();
                        }
                        else {
                            Toast.makeText(SignInActivity.this, "Email Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });

            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();
        });
    }

    private void makeErrorDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Please fill all the Details!");
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void makeDialog(String email, String pass) {
        mFirebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, "User signedIn successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignInActivity.this, SetProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(SignInActivity.this, "SignIn Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}