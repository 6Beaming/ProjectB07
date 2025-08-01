package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

// This page will only show on the first sign up -> can be used to confirm if this is user's sign up
// uid has to exist on this activity
// Ask the user to type their PIN twice to confirm
// Store it as "pin_"+uid as key in SharedPreferences to make sure each user reserves a unique PIN
public class PinSetupActivity extends AppCompatActivity {
    private EditText pinFirst;
    private EditText pinSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        pinFirst = findViewById(R.id.pinFirst);
        pinSecond = findViewById(R.id.pinSecond);
        Button buttonNext = findViewById(R.id.buttonNext);

        PinManager pinManager = new PinManager(this); //utility class

        buttonNext.setOnClickListener(v -> {
            String p1 = pinFirst.getText().toString();
            String p2 = pinSecond.getText().toString();
            if (p1.length() != 4 && p1.length() != 6) {
                pinFirst.setError("PIN must be 4 or 6 digits");
                return;
            }
            if (!p1.equals(p2)) {
                pinSecond.setError("PINs do not match");
                return;
            }
            // On success -> store
            try {
                pinManager.storePin(uid, p1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(this,"Saved! Here's your initial questionnaire", Toast.LENGTH_SHORT).show();
            // Then, go to main
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
