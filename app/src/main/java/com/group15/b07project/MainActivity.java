package com.group15.b07project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if disclaimer is shown and accepted.(from SharedPreference)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean disclaimerShown = prefs.getBoolean("disclaimer_shown", false);
        // if not read
        if (!disclaimerShown) {
            Intent intent = new Intent(this, DisclaimerActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // if is read
        setContentView(R.layout.activity_main);

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