package com.group15.b07project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        Button buttonQuestionnaire = view.findViewById(R.id.buttonQuestionnaire);
        Button buttonPlan = view.findViewById(R.id.buttonPlan);
        Button buttonEmergencyInfo = view.findViewById(R.id.buttonEmergencyInfo);
        Button buttonSupport = view.findViewById(R.id.buttonSupport);
        Button buttonLogout = view.findViewById(R.id.buttonLogout);

        buttonQuestionnaire.setOnClickListener(v -> loadFragment(new QuestionnaireFragment()));

        buttonPlan.setOnClickListener(v -> loadFragment(new PlanGenerationFragment()));

        buttonEmergencyInfo.setOnClickListener(v -> loadFragment(new StorageOfEmergencyInfoFragment()));

        buttonSupport.setOnClickListener(v -> loadFragment(new SupportConnectionFragment()));

        //buttonLogout.setOnClickListener(v -> loadFragment(new LogoutFragment()));

        return view;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
