package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Press login with username -> go to LoginActivity
// Press Login with PIN -> go to PinLoginActivity
public class AuthChoiceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_choice);

        findViewById(R.id.buttonByPin).setOnClickListener(v -> {
            startActivity(new Intent(this, PinLoginActivity.class));
            finish();
        });
        findViewById(R.id.buttonByPwd).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
