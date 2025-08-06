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
 * Fragment to display and manage Safe Locations CRUD operations.
 */
public class SafeLocationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<SafeLocation> items;
    private SafeLocationsAdapter adapter;
    private DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_safe_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Build Firebase path: /users/{uid}/EmergencyInfo/SafeLocations
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("EmergencyInfo")
                .child("SafeLocations");

        // RecyclerView & adapter setup
        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd       = view.findViewById(R.id.fab_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        items   = new ArrayList<>();
        adapter = new SafeLocationsAdapter(items, new SafeLocationsAdapter.OnItemClickListener() {
            @Override public void onEdit(SafeLocation loc) { showEditDialog(loc); }
            @Override public void onDelete(SafeLocation loc) { ref.child(loc.getId()).removeValue(); }
        });
        recyclerView.setAdapter(adapter);

        // Listen for data changes and update UI
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    SafeLocation loc = ds.getValue(SafeLocation.class);
                    if (loc != null) {
                        loc.setId(ds.getKey());
                        items.add(loc);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // FAB click -> add new location dialog
        fabAdd.setOnClickListener(v -> showAddDialog());

        // Back button to return to Emergency Info menu
        ImageButton btnBack = view.findViewById(R.id.button_back_to_emergencyInfo);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /** Show dialog to add a new safe location. */
    private void showAddDialog() {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_safe_location, null);
        EditText etAddr  = v.findViewById(R.id.etAddress);
        EditText etNotes = v.findViewById(R.id.etNotes);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Safe Location")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    String id = ref.push().getKey();
                    SafeLocation sl = new SafeLocation(
                            id,
                            etAddr.getText().toString().trim(),
                            etNotes.getText().toString().trim()
                    );
                    ref.child(id).setValue(sl);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Show dialog to edit an existing safe location. */
    private void showEditDialog(SafeLocation sl) {
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_safe_location, null);
        EditText etAddr  = v.findViewById(R.id.etAddress);
        EditText etNotes = v.findViewById(R.id.etNotes);
        etAddr.setText(sl.getAddress());
        etNotes.setText(sl.getNotes());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Safe Location")
                .setView(v)
                .setPositiveButton("Update", (d, w) -> {
                    sl.setAddress(etAddr.getText().toString().trim());
                    sl.setNotes(etNotes.getText().toString().trim());
                    ref.child(sl.getId()).setValue(sl);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}