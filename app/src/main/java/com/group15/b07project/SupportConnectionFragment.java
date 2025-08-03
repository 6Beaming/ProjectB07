package com.group15.b07project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment to display support services based on user-selected city.
 */
public class SupportConnectionFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_support_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Load JSON from raw resource
        InputStream is = requireContext().getResources().openRawResource(R.raw.services_directory);
        InputStreamReader reader = new InputStreamReader(is);

        // Parse JSON into HashMap<String, List<ServiceEntry>>
        Type type = new TypeToken<HashMap<String, List<ServiceEntry>>>() {}.getType();
        HashMap<String, List<ServiceEntry>> servicesMap = new Gson().fromJson(reader, type);

        // Get user-selected city from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", getContext().MODE_PRIVATE);
        String selectedCity = prefs.getString("user_city", "Toronto");

        // Retrieve the list for this city
        List<ServiceEntry> entries = servicesMap.get(selectedCity);

        // Bind to RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvServices);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new ServiceAdapter(entries));
    }
}
