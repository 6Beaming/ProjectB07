package com.group15.b07project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of emergency contacts.
 * Supports edit and delete actions via a provided listener.
 */
public class EmergencyContactsAdapter
        extends RecyclerView.Adapter<EmergencyContactsAdapter.VH> {
    /**
     * Interface to handle edit and delete button clicks for each item.
     */
    public interface OnItemClickListener {
        /**
         * Called when the edit button for a contact is clicked.
         * @param contact The EmergencyContact to edit.
         */
        void onEdit(EmergencyContact contact);
        /**
         * Called when the delete button for a contact is clicked.
         * @param contact The EmergencyContact to delete.
         */
        void onDelete(EmergencyContact contact);
    }

    // List of contacts to display
    private List<EmergencyContact> items;
    // Listener to forward edit/delete events
    private OnItemClickListener listener;

    /**
     * Constructor for the adapter.
     * @param items Initial data list of EmergencyContact objects.
     * @param listener Callback listener for item actions.
     */
    public EmergencyContactsAdapter(List<EmergencyContact> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each RecyclerView row
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Bind data to the view holder at the given position
        EmergencyContact c = items.get(position);
        holder.tvName.setText(c.getName());
        holder.tvRelationship.setText(c.getRelationship());
        holder.tvPhone.setText(c.getPhone());
        // Set click listeners to forward events to the fragment
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        // Return total number of items in the list
        return items.size();
    }

    /**
     * ViewHolder class to cache view references for each item.
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvRelationship, tvPhone;
        ImageButton btnEdit, btnDelete;

        VH(View itemView) {
            super(itemView);
            // Initialize view references
            tvName = itemView.findViewById(R.id.tvName);
            tvRelationship = itemView.findViewById(R.id.tvRelationship);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
