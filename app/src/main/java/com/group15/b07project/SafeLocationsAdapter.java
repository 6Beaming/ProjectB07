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
 * Adapter to bind SafeLocation data to the RecyclerView.
 */
public class SafeLocationsAdapter extends RecyclerView.Adapter<SafeLocationsAdapter.VH> {
    /**
     * Listener interface for edit and delete actions.
     */
    public interface OnItemClickListener {
        void onEdit(SafeLocation location);
        void onDelete(SafeLocation location);
    }

    private List<SafeLocation> items;
    private OnItemClickListener listener;

    public SafeLocationsAdapter(List<SafeLocation> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate each row layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_safe_location, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Bind SafeLocation object to the view
        SafeLocation loc = items.get(position);
        holder.tvAddress.setText(loc.getAddress());
        holder.tvNotes.setText(loc.getNotes());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(loc));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(loc));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder caches view references for performance.
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvAddress, tvNotes;
        ImageButton btnEdit, btnDelete;

        VH(View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvNotes   = itemView.findViewById(R.id.tvNotes);
            btnEdit   = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}