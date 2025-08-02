package com.group15.b07project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    private List<String> files;

    public DocumentAdapter(List<String> files) {
        this.files = files;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_adapter,parent,false);
        return new DocumentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.fileName.setText(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageButton edit_button, delete_button;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.textview_file_name);
            edit_button=itemView.findViewById(R.id.button_edit_file);
            delete_button=itemView.findViewById(R.id.button_delete_file);
        }
    }
}
