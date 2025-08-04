package com.group15.b07project;

import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // check if disclaimer is shown and accepted.(from SharedPreference)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean disclaimerShown = prefs.getBoolean(uid + "disclaimer_shown", false);
        if (!disclaimerShown) {
            Intent intent = new Intent(this, DisclaimerActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // if disclaimer is already read
        setContentView(R.layout.activity_main);
        setupEmergencyExitButton();

        // decide whether to show QuestionnaireFragment or HomeFragment
        if (savedInstanceState == null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("newUser");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isNewUser = snapshot.getValue(Boolean.class); // already set true on the first log in
                    if (isNewUser == null || isNewUser) {
                        loadFragment(new QuestionnaireFragment()); // questionnaire will then set the ref false
                    } else {
                        loadFragment(new HomeFragment());
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Error loading newUser: " + error.getMessage());
                    loadFragment(new HomeFragment()); // if any error on Firebase, then go to HomeFragment
                }
            });
        }
    }

    private void setupEmergencyExitButton() {
        FloatingActionButton exitFab = findViewById(R.id.fab_emergency_exit);
        exitFab.setOnClickListener(v -> {
            // Create an intent to open Google in the device’s default browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com"));
            // Launch the browser in a new task and clear this app’s task stack
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(browserIntent);
            finishAndRemoveTask(); // Finish this activity and remove its task from the Recent Apps list
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        // Note transaction.addToBackStack(null); is removed
        transaction.commit();
    }
}