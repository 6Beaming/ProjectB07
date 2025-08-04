package com.group15.b07project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to manage Medication entries (CRUD) via Firebase.
 */
public class MedicationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<Medication> items;
    private MedicationsAdapter adapter;
    private DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Firebase path: /users/{uid}/EmergencyInfo/Medications
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("EmergencyInfo")
                .child("Medications");

        // Setup RecyclerView & adapter
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd       = view.findViewById(R.id.fab_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        items   = new ArrayList<>();
        adapter = new MedicationsAdapter(items, new MedicationsAdapter.OnItemClickListener() {
            @Override public void onEdit(Medication m) { showEditDialog(m); }
            @Override public void onDelete(Medication m) { ref.child(m.getId()).removeValue(); }
        });
        recyclerView.setAdapter(adapter);

        // Listen for data changes
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Medication m = ds.getValue(Medication.class);
                    if (m != null) {
                        m.setId(ds.getKey());
                        items.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Add new medication
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Back to Emergency Info menu
        ImageButton btnBack = view.findViewById(R.id.button_back_to_emergencyInfo);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /** Prompt user to add a new Medication. */
    private void showAddDialog() {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_medication, null);
        EditText etName   = v.findViewById(R.id.etName);
        EditText etDosage = v.findViewById(R.id.etDosage);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Medication")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    String id = ref.push().getKey();
                    Medication m = new Medication(
                            id,
                            etName.getText().toString().trim(),
                            etDosage.getText().toString().trim()
                    );
                    ref.child(id).setValue(m);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Prompt user to edit an existing Medication. */
    private void showEditDialog(Medication m) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_medication, null);
        EditText etName   = v.findViewById(R.id.etName);
        EditText etDosage = v.findViewById(R.id.etDosage);
        etName.setText(m.getName());
        etDosage.setText(m.getDosage());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Medication")
                .setView(v)
                .setPositiveButton("Update", (d, w) -> {
                    m.setName(etName.getText().toString().trim());
                    m.setDosage(etDosage.getText().toString().trim());
                    ref.child(m.getId()).setValue(m);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}