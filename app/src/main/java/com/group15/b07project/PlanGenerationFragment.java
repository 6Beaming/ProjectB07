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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class PlanGenerationFragment extends Fragment {
    private RecyclerView recyclerView;
    private TipAdapter tipAdapter;
    private List<String> tips = new ArrayList<>();
    private String status;

    public PlanGenerationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_generation, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerViewTips);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tipAdapter = new TipAdapter(tips);
        recyclerView.setAdapter(tipAdapter);

        tips.clear();
        tips.add("Personalized Plan");
        loadTips(); // or from arguments


    }

    private void loadTips() {
        String json = loadJSONFromAsset(getContext(), "questions.json");
        Gson gson = new Gson();
        QuestionsBundle questions = gson.fromJson(json, QuestionsBundle.class);

        /*
        Load Specific Answer of this user
         */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get user
        assert user != null;
        String uid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("answers")
                .child(uid)
                .child("questionnaire");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                loadWarmupTips(tips, questions, snapshot); //this also sets value of status
                loadBranchTips(tips,questions,snapshot);
                loadFollowupTips(tips,questions,snapshot);



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

    private void loadWarmupTips(List<String> tips,
                                @NonNull QuestionsBundle questions, @NonNull DataSnapshot snapshot) {
        List<Question> warmup = questions.warmUp;

        status = snapshot.child("status").getValue(String.class);
        for (Question question :
                warmup) {
            if (question.id.equals("live_with")) {
                String answer=Objects.requireNonNull(snapshot.child(question.id).getValue(String.class));
                if (answer.equals("Family")||answer.equals("Roommates")){
                    tips.add(
                            ((List<String>)question.tips).get(0).replace("{family/roommates}",answer)
                    );
                } else if (answer.equals("Alone")) {
                    tips.add(
                            ((List<String>)question.tips).get(1)
                    );
                }else {
                    String safe_room=snapshot.child(question.id).getValue(String.class);
                    assert safe_room != null;
                    tips.add(
                            ((List<String>)question.tips).get(2).replace("{safe_room}",safe_room)
                    );
                }
            }else{
                processTip(tips,snapshot,question);
            }

        }
    }

    private void loadBranchTips(List<String> tips,
                                @NonNull QuestionsBundle questions, @NonNull DataSnapshot snapshot){
        List<Question> branchQuestions=questions.branch.get(status);
        assert branchQuestions != null;
        for (Question question:
             branchQuestions) {
                if (question.type.equals("multiple")){
                    GenericTypeIndicator<List<String>> typeIndicator = new GenericTypeIndicator<List<String>>() {};
                    List<String> answer=Objects.requireNonNull(snapshot.child(question.id).getValue(typeIndicator));
                    StringJoiner joiner=new StringJoiner(" ,");
                    for (int i = 0; i < answer.size(); i++) {
                        joiner.add(answer.get(i));
                    }
                    String cat_answer=joiner.toString();
                    tips.add(
                            ((String)question.tips).replace("{abuse_type}",cat_answer)
                    );
                }else {
                    processTip(tips,snapshot,question);
                }

        }
    }

    private void loadFollowupTips(List<String> tips,
                                  @NonNull QuestionsBundle questions, @NonNull DataSnapshot snapshot){
        Question followupQuestion=questions.followUp.get(0);
        String answer=snapshot.child(followupQuestion.id).getValue(String.class);
        assert answer != null;
        tips.add(
                ((String) followupQuestion.tips).replace("{support_choice}",answer)
        );
    }

    private void processTip(List<String> tips, DataSnapshot snapshot,
                            Question question){
        List<String> type_insert= Arrays.asList("dropdown","text","date");

        if (type_insert.contains(question.type)){
            insertAnswer(tips,snapshot,question);
        }else if (question.type.equals("single")){
            chooseTip(tips,snapshot,question);
        }else if (question.type.equals("single+text")){
            chooseAndInsert(tips,snapshot,question);
        }

    }
    private void insertAnswer(@NonNull List<String> tips, @NonNull DataSnapshot snapshot,
                              @NonNull Question question) { //precondition: type is dropdown,date,text. except safe_room
        String answer=Objects.requireNonNull(snapshot.child(question.id).getValue(String.class));
        tips.add(
                ((String)question.tips).replace("{answer}", answer)
        );

    }

    private void chooseTip(@NonNull List<String> tips, @NonNull DataSnapshot snapshot,
                           @NonNull Question question){//precondition: type is single
        String answer=Objects.requireNonNull(snapshot.child(question.id).getValue(String.class));
        int choice_index=question.options.indexOf(answer);
        tips.add(
                ((List<String>)question.tips).get(choice_index)
        );
    }

    private void chooseAndInsert(List<String> tips, DataSnapshot snapshot,
                                 Question question){//type must be single+text
        String answer=Objects.requireNonNull(snapshot.child(question.id).getValue(String.class));
        if (question.options.indexOf(answer)==1){
            tips.add(
                    ((List<String>)question.tips).get(1)    //because all followup occur in first choice
            );
        }else {
            String text = Objects.requireNonNull(snapshot.child(question.id + "_text").getValue(String.class));
            tips.add(
                    ((List<String>)question.tips).get(0).replace("{answer}",text)
            );
        }
    }

}