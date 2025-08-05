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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Objects;

import android.util.Log;

/**
 * A Fragment that displays support services based on the user's selected city,
 * which is read from Firebase Realtime Database under "answers/{userId}/city".
 */
public class SupportConnectionFragment extends Fragment {
    private RecyclerView rvServices;
    private ServiceAdapter adapter;
    private HashMap<String, List<ServiceEntry>> servicesMap;
    private DatabaseReference answersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_connection, container, false);

        // 1) Parse JSON directory of services
        InputStream is = requireContext()
                .getResources()
                .openRawResource(R.raw.services_directory);
        InputStreamReader reader = new InputStreamReader(is);
        Type type = new TypeToken<HashMap<String, List<ServiceEntry>>>() {}.getType();
        servicesMap = new Gson().fromJson(reader, type);

        // 2) Initialize RecyclerView
        rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.adapter = new ServiceAdapter(new ArrayList<>());
        rvServices.setAdapter(this.adapter);

        // 3) Set up Firebase reference to this user's answers
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        answersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("questionnaire");
        Log.d("SupportConnFrag", "answersRef path: " + answersRef.toString());

        // 4) Load the actual selected city and refresh the list
        loadCityFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh each time fragment becomes visible
        loadCityFromFirebase();
    }

    /**
     * Reads the 'city' field from Firebase and updates the adapter.
     */
    private void loadCityFromFirebase() {
        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("SupportConnFrag", "snapshot="+snapshot.getValue());
                // Toast.makeText(requireContext(), "raw="+snapshot.getValue(), Toast.LENGTH_SHORT).show();

                String city = snapshot.child("city").getValue(String.class);
                Log.d("SupportConnFrag", "city="+city);
                if (city != null && servicesMap.containsKey(city)) {
                    List<ServiceEntry> list = servicesMap.get(city);
                    adapter.updateData(list);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
