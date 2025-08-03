package com.group15.b07project;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;
    String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if disclaimer is shown and accepted.(from SharedPreference)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean disclaimerShown = prefs.getBoolean(uid + "disclaimer_shown", false);
        // if not read
        if (!disclaimerShown) {
            Intent intent = new Intent(this, DisclaimerActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // if is read
        setContentView(R.layout.activity_main);

        // Emergency Exit button initialization start
        FloatingActionButton exitFab = findViewById(R.id.fab_emergency_exit);
        exitFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to open Google in the device’s default browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com"));
                // Launch the browser in a new task and clear this app’s task stack
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // Start the browser activity
                startActivity(browserIntent);
                // Finish this activity and remove its task from the Recent Apps list
                finishAndRemoveTask();
            }
        });
        // Emergency Exit button initialization complete

        db = FirebaseDatabase.getInstance("https://projectb07-62fc7-default-rtdb.firebaseio.com/");
        DatabaseReference myRef = db.getReference("b07project");

//        myRef.setValue("B07 Demo!");
        myRef.child("movies").setValue("B07 Demo!");

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
        // show questionnaire -- Need refactoring
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new QuestionnaireFragment())
                .commit();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // This line enables "Back" to return to previous fragment
                                                // Remove it if you want to use Back to exit the app instead
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}