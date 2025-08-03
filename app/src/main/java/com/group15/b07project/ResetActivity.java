package com.group15.b07project;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private EditText email;
    private Button reset;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        email = findViewById(R.id.eEmail);
        reset = findViewById(R.id.resetButton);
        auth = FirebaseAuth.getInstance();

        reset.setOnClickListener(v -> {
            String emailed = email.getText().toString().trim();

            if (TextUtils.isEmpty(emailed)) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.sendPasswordResetEmail(emailed)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send the reset email, please check if the account exists or try again", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}