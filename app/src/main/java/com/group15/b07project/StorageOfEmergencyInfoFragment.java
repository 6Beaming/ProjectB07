package com.group15.b07project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class StorageOfEmergencyInfoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_storage_of_emergency_info,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton home_button=view.findViewById(R.id.buttonEmergencyInfoHome);
        Button document_button=view.findViewById(R.id.button_documents_to_pack);
        Button emergency_contacts_button=view.findViewById(R.id.button_emergency_contacts);
        Button safe_location_button=view.findViewById(R.id.button_safe_locations);
        Button medications_button=view.findViewById(R.id.button_medications);

        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new HomeFragment());
            }
        });
        document_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new DocumentsToPackFragment());
            }
        });
        emergency_contacts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {loadFragment(new EmergencyContactsFragment());}
        });
        safe_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {loadFragment(new SafeLocationsFragment());}
        });
        medications_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {loadFragment(new MedicationsFragment());}
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
