package com.group15.b07project;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Objects;



// Displays support services based on the user's selected city
public class SupportConnectionFragment extends Fragment {
    private ServiceAdapter adapter;
    private HashMap<String, List<ServiceEntry>> servicesMap;
    private DatabaseReference answersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_connection, container, false);

        // Parse JSON directory of services
        Type type = new TypeToken<HashMap<String, List<ServiceEntry>>>(){}.getType();
        servicesMap =  ParseJson.loadJson(requireContext(), "services_directory.json", type);

        // Initialize RecyclerView
        RecyclerView rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.adapter = new ServiceAdapter(new ArrayList<>());
        rvServices.setAdapter(this.adapter);

        // Set up Firebase reference to this user's answers
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        answersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("questionnaire");

        // Load the actual selected city and refresh the list
        loadCityFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh each time fragment becomes visible
        loadCityFromFirebase();
    }

    // Reads the city field from Firebase and updates the adapter.
    private void loadCityFromFirebase() {
        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String city = snapshot.child("city").getValue(String.class);
                if (city != null && servicesMap.containsKey(city)) {
                    List<ServiceEntry> list = servicesMap.get(city);
                    adapter.updateData(list);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
