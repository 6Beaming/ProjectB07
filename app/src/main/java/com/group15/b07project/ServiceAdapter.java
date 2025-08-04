package com.group15.b07project;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.group15.b07project.R;
import java.util.List;

/**
 * Adapter for displaying each service entry in a card layout.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.VH> {
    private List<ServiceEntry> list;

    /**
     * Replace the current data list and refresh the RecyclerView.
     */
    public void updateData(List<ServiceEntry> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }
    public ServiceAdapter(List<ServiceEntry> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate item layout from XML
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ServiceEntry e = list.get(position);
        holder.name.setText(e.name);
        holder.phone.setText(e.phone);
        holder.url.setText(e.url);

        // Click to dial number
        holder.phone.setOnClickListener(v -> {
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + e.phone));
            v.getContext().startActivity(dial);
        });

        // Click to open URL
        holder.url.setOnClickListener(v -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(e.url));
            v.getContext().startActivity(browser);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder for service entry items
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView name, phone, url;

        public VH(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            phone = v.findViewById(R.id.phone);
            url = v.findViewById(R.id.url);
        }
    }
}