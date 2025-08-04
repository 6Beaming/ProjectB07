package com.group15.b07project;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    private final List<DocMetadataStructure> files;
    private final OnFileItemClickListener listener;

    public interface OnFileItemClickListener {
        void onDownloadClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public DocumentAdapter(List<DocMetadataStructure> files, OnFileItemClickListener listener) {
        this.files = files;
        this.listener = listener;
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
        holder.fileName.setText(files.get(position).getDocsData().getTitle());
        holder.time.setText(files.get(position).getDocsData().getUploadDate());
        Log.d("ADAPTER_BIND", "Binding item at position " + position + ": title = " + files.get(position).getDocsData().getTitle());
    }

    @Override
    public int getItemCount() {
        Log.d("ADAPTER_COUNT", "getItemCount(): " + files.size());
        return files.size();
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder {
        Button fileName;
        ImageButton edit_button, delete_button;
        TextView time;


        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.file_name);
            edit_button=itemView.findViewById(R.id.button_edit_file);
            delete_button=itemView.findViewById(R.id.button_delete_file);
            time=itemView.findViewById(R.id.timetext);

            fileName.setOnClickListener(v->{
                int pos=getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDownloadClick(pos);
                }
            });
            edit_button.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(pos);
                }
            });

            delete_button.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(pos);
                }
            });

        }
    }
}
