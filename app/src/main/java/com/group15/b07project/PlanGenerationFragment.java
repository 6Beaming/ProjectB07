package com.group15.b07project;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanGenerationFragment extends Fragment {
    private RecyclerView recyclerView;
    private TipAdapter tipAdapter;
    private List<String> tips = new ArrayList<>();

    public PlanGenerationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_generation, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTips);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tipAdapter = new TipAdapter(tips);
        recyclerView.setAdapter(tipAdapter);

        loadTips(); // or from arguments
        return view;
    }

    private void loadTips() {
        String json = loadJSONFromAsset(getContext(), "questions.json");
        Gson gson = new Gson();
        QuestionsBundle questions = gson.fromJson(json,QuestionsBundle.class);

        /*
        Load Specific Answer of this user
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("answers").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Deserialize manually or using model class
                List<Answer> warmUpAnswers = new ArrayList<>();
                for (DataSnapshot qSnap : snapshot.child("warmUp").getChildren()) {
                    Answer ans = qSnap.getValue(Answer.class);
                    warmUpAnswers.add(ans);
                }

                // Do the same for "branch" and "followUp"


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading answers: " + error.getMessage());
            }
        });

    }
    public String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


}
