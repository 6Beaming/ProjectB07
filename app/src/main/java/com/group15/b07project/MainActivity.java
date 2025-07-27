package com.group15.b07project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
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