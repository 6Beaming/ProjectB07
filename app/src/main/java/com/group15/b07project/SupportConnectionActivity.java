package com.group15.b07project;

// Android framework imports
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

// AndroidX imports
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Gson imports
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Java imports
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

// App-specific imports
import com.group15.b07project.R;
import com.group15.b07project.ServiceEntry;
import com.group15.b07project.ServiceAdapter;

/**
 * Activity to display support services based on user-selected city.
 */
public class SupportConnectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_connection);

        // Load JSON from raw resource
        InputStream is = getResources().openRawResource(R.raw.services_directory);
        InputStreamReader reader = new InputStreamReader(is);

        // Parse JSON into HashMap<String, List<ServiceEntry>>
        Type type = new TypeToken<HashMap<String, List<ServiceEntry>>>() {}.getType();
        HashMap<String, List<ServiceEntry>> servicesMap = new Gson().fromJson(reader, type);

        // Get user-selected city from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String selectedCity = prefs.getString("user_city", "Toronto");

        // Retrieve the list for this city
        List<ServiceEntry> entries = servicesMap.get(selectedCity);

        // Bind to RecyclerView
        RecyclerView rv = findViewById(R.id.rvServices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new ServiceAdapter(entries));
    }
}