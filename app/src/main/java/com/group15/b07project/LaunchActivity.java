package com.group15.b07project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        }
        else {
            String uid = user.getUid();
            String pinKey = "pin_" + uid;
            SharedPreferences prefs = getApplicationContext()
                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

            if (!prefs.contains(pinKey)) {
                // signed in but no PIN yet -> ask log in again
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                // signed in and has PIN -> show unlock options (Auth choice page)
                startActivity(new Intent(this, AuthChoiceActivity.class));
            }
        }

        // finish so Back never returns here
        finish();
    }
}
