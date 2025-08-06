package com.group15.b07project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


//Adapter for displaying Medication items with edit/delete actions.
public class MedicationsAdapter extends RecyclerView.Adapter<MedicationsAdapter.VH> {
    public interface OnItemClickListener {
        void onEdit(Medication med);
        void onDelete(Medication med);
    }

    private final List<Medication> items;
    private final OnItemClickListener listener;

    public MedicationsAdapter(List<Medication> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Medication med = items.get(position);
        holder.tvName.setText(med.getName());
        holder.tvDosage.setText(med.getDosage());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(med));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(med));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //ViewHolder caches views for a medication item.
    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage;
        ImageButton btnEdit, btnDelete;

        VH(View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            btnEdit  = itemView.findViewById(R.id.btnEdit);
            btnDelete= itemView.findViewById(R.id.btnDelete);
        }
    }
}