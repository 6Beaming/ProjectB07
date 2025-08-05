package com.group15.b07project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class DisclaimerActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        Button understandBtn = findViewById(R.id.btn_understand);
        understandBtn.setOnClickListener(v -> {
            //Storing (status: understood) into SharedPreference
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean(uid + "disclaimer_shown", true).apply();
            Intent intent = new Intent(DisclaimerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
