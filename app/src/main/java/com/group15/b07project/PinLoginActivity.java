package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

// An activity that is launched by AuthChoiceActivity(prerequisite is uid is Non null)
// If PIN is verified, then go to MainActivity
public class PinLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        EditText pinInput  = findViewById(R.id.pinInput);
        Button buttonUnlock = findViewById(R.id.buttonUnlock);
        Button buttonBack = findViewById(R.id.buttonBack);
        PinManager pinManager = new PinManager(this);

        buttonUnlock.setOnClickListener(v -> {
            String typed  = pinInput.getText().toString();
            if (pinManager.verifyPin(uid, typed)) {
                // PIN verified -> main
                startActivity(new Intent(this, MainActivity.class));
                finish(); // so if press back, user won't go back to here
            } else {
                pinInput.setError("Wrong PIN");
            }
        });

        buttonBack.setOnClickListener(v -> {
            startActivity(new Intent(this, AuthChoiceActivity.class));
            finish();
        });
    }
}
