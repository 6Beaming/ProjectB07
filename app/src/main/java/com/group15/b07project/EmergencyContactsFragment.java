package com.group15.b07project;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Fragment to display and manage a list of emergency contacts.
public class EmergencyContactsFragment extends Fragment {
    // Data list and adapter for RecyclerView
    private List<EmergencyContact> items;
    private EmergencyContactsAdapter adapter;
    // Reference to Firebase node for storing emergency contacts
    private DatabaseReference ref;
    private int brown_grey;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emergency_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the current user ID from Firebase Authentication
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        // Build the database reference path: /users/{uid}/EmergencyInfo/EmergencyContacts
        ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("EmergencyInfo")
                .child("EmergencyContacts");

        // Initialize RecyclerView and FAB
        // RecyclerView to show the list of contacts
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        // FloatingActionButton to add a new contact
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        brown_grey = ContextCompat.getColor(requireContext(), R.color.browngrey);

        // Prepare data list and adapter with edit/delete callbacks
        items = new ArrayList<>();
        adapter = new EmergencyContactsAdapter(items, new EmergencyContactsAdapter.OnItemClickListener() {
            @Override
            public void onEdit(EmergencyContact c) {
                // Open dialog to edit the selected contact
                showEditDialog(c);
            }

            @Override
            public void onDelete(EmergencyContact c) {
                // Remove the selected contact from Firebase
                ref.child(c.getId()).removeValue();
            }
        });
        recyclerView.setAdapter(adapter);

        // Set up FAB click to add new contact
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Listen for database changes and update UI accordingly
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();  // Clear old data
                for (DataSnapshot ds : snapshot.getChildren()) {
                    EmergencyContact c = ds.getValue(EmergencyContact.class);
                    if (c != null) {
                        c.setId(ds.getKey());  // Store the database key in the model
                        items.add(c);
                    }
                }
                adapter.notifyDataSetChanged();  // Refresh list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors (not implemented)
            }
        });

        // Set up the back button to return to the previous menu
        ImageButton btnBack = view.findViewById(R.id.button_back_to_emergencyInfo);
        btnBack.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .popBackStack());
    }

    //Show a dialog for adding a new emergency contact.
    private void showAddDialog() {
        // Inflate the custom dialog layout
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_emergency_contact, null);
        // Bind EditText fields
        EditText etName  = v.findViewById(R.id.etName);
        EditText etRel   = v.findViewById(R.id.etRelationship);
        EditText etPhone = v.findViewById(R.id.etPhone);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Emergency Contact")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Create a new key and model object
                    String id = ref.push().getKey();
                    EmergencyContact c = new EmergencyContact(
                            id,
                            etName.getText().toString().trim(),
                            etRel.getText().toString().trim(),
                            etPhone.getText().toString().trim()
                    );
                    // Save to Firebase
                    ref.child(Objects.requireNonNull(id)).setValue(c);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    //Show a dialog to edit an existing emergency contact.

    private void showEditDialog(EmergencyContact c) {
        // Inflate the same layout but pre-fill fields
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_emergency_contact, null);
        EditText etName  = v.findViewById(R.id.etName);
        EditText etRel   = v.findViewById(R.id.etRelationship);
        EditText etPhone = v.findViewById(R.id.etPhone);
        etName.setText(c.getName());
        etName.setTextColor(brown_grey);
        etRel.setText(c.getRelationship());
        etRel.setTextColor(brown_grey);
        etPhone.setText(c.getPhone());
        etPhone.setTextColor(brown_grey);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Emergency Contact")
                .setView(v)
                .setPositiveButton("Update", (dialog, which) -> {
                    // Update model and push changes
                    c.setName(etName.getText().toString().trim());
                    c.setRelationship(etRel.getText().toString().trim());
                    c.setPhone(etPhone.getText().toString().trim());
                    ref.child(c.getId()).setValue(c);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
