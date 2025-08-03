package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText editedEmail;
    private EditText editedPassword;
    private EditText confirmedPassword;
    private Button signingUp;
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Auth = FirebaseAuth.getInstance();
        editedEmail = findViewById(R.id.edited_email);
        editedPassword = findViewById(R.id.edited_password);
        confirmedPassword = findViewById(R.id.confirmed_password);
        signingUp = findViewById(R.id.btn_signup);

        signingUp.setOnClickListener(v -> {
            String email = editedEmail.getText().toString();
            String password = editedPassword.getText().toString();
            String confirmed = confirmedPassword.getText().toString();

            //check proper inputs
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Email address cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(confirmed)) {
                Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmed)) {
                Toast.makeText(this, "Please enter same passwords", Toast.LENGTH_SHORT).show();
                return;
            }

            //Storing email and password
            Auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = Auth.getCurrentUser();

                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifying -> {
                                            if (verifying.isSuccessful()) {
                                                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                                                //directing to login page(login by password page)
                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Unable to send verification email.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "This Email has already been used.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}