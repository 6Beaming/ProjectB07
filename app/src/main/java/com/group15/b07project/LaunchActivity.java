package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LaunchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            String uid = user.getUid();
             PinManager pinManager = new PinManager(this);

            if (!pinManager.hasPin(uid)) {
                // signed in but no PIN yet -> ask log in again
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                // signed in and has PIN -> show unlock options (Auth choice page)
                startActivity(new Intent(this, AuthChoiceActivity.class));
                finish();
            }
        }
    }
}
