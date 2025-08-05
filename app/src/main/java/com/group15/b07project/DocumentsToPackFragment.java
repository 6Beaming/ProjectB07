package com.group15.b07project;

import static com.group15.b07project.FirebaseFileHelper.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.UploadTask;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Fragment for managing and displaying documents that users need to pack.
 * Allows users to upload, edit, delete, and download files stored in Firebase.
 */
public class DocumentsToPackFragment extends Fragment implements DocumentAdapter.OnFileItemClickListener {
    private List<DocMetadataStructure> files;
    private ProgressBar progressBar;
    private DocumentAdapter documentAdapter;
    private String uid;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents_to_pack,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext());
        }


        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null) uid=user.getUid();

        RecyclerView recyclerView_docs = view.findViewById(R.id.recyclerview_docs);
        FloatingActionButton fab_add_file = view.findViewById(R.id.fab_add_docs);
        ImageButton back_to_emergency_info = view.findViewById(R.id.button_back_to_emegencyInfo);
        progressBar = view.findViewById(R.id.upload_progress_bar);

        recyclerView_docs.setLayoutManager(new LinearLayoutManager(getContext()));
        files = new ArrayList<>();
        documentAdapter = new DocumentAdapter(files, this);
        recyclerView_docs.setAdapter(documentAdapter);

        updateFilesList(uid);

        // Launches file picker and handles metadata input
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == MainActivity.RESULT_OK && result.getData() != null) {
                        Toast.makeText(getContext(), "File selected", Toast.LENGTH_SHORT).show();
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            //ask for title and description
                            getChildFragmentManager().setFragmentResultListener(
                                    "metadata_request_key",
                                    this,
                                    (requestKey, result1) -> {
                                        String title = result1.getString("title");
                                        String description = result1.getString("description");
                                        uploadFileToDatabase(fileUri, title, description);
                                    }
                            );
                            showMetadataBottomSheet(null, null);
                            //start uploading
                        }
                    }
                }
        );
        //user choose file, then set title, description
        fab_add_file.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });
        back_to_emergency_info.setOnClickListener(v -> loadFragment(new StorageOfEmergencyInfoFragment()));
    }


    @Override
    public void onDownloadClick(int position) {
        String url=files.get(position).getDocsData().getDownloadUrl();
        String title=files.get(position).getDocsData().getTitle()+"."+getFileExtension(files.get(position).getDocsData().getStoragePath());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription("Please wait...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

        DownloadManager manager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

        manager.enqueue(request);
        Toast.makeText(getContext(),"Download started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(int position) {
        String fileId=files.get(position).getDocId();
        String title_old=files.get(position).getDocsData().getTitle();
        String description_old=files.get(position).getDocsData().getDescription();
        getChildFragmentManager().setFragmentResultListener(
                "metadata_request_key",
                this,
                (requestKey, result) -> {
                    String title = result.getString("title");
                    String description = result.getString("description");
                    editFileMetadata(uid,fileId,title,description);
                    updateFilesList(uid);
                }
        );
        showMetadataBottomSheet(title_old,description_old);
    }

    @Override
    public void onDeleteClick(int position) {
        String fileId=files.get(position).getDocId();
        String storagePath=files.get(position).getDocsData().getStoragePath();
        String title_time=files.get(position).getDocsData().getTitle()+files.get(position).getDocsData().getUploadDate();
        deleteFile(uid, fileId, storagePath, new UploadCallback(){
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(),"Deleted file "+title_time,Toast.LENGTH_SHORT).show();
                updateFilesList(uid);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),"File delete failed",Toast.LENGTH_SHORT).show();
            }
        });

    }
    /**
     * Displays bottom sheet for entering or editing document metadata.
     * @param title Existing title (if editing), or null.
     * @param description Existing description (if editing), or null.
     */
    private void showMetadataBottomSheet(String title,String description) {        //load sheet for adding file
        EditDocumentFragment sheet = EditDocumentFragment.newInstance(title, description);
        sheet.show(getChildFragmentManager(), "docSheet");
    }
    /**
     * Fetches updated list of documents from Firebase and refreshes the adapter.
     * @param uid User ID whose documents are being fetched.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateFilesList(String uid){
        files.clear();

        DatabaseReference docsRef=FirebaseDatabase.getInstance().getReference("users").child(uid).child("Documents");
        docsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot docsSnapshot:
                     snapshot.getChildren()) {
                    Log.d("FILES_DEBUG", "Found docId: " + docsSnapshot.getKey());
                    String docId=docsSnapshot.getKey();
                    DocsDataStructure docsdataStructure=docsSnapshot.getValue(DocsDataStructure.class);
                    DocMetadataStructure docMetadataStructure=new DocMetadataStructure(docId,docsdataStructure);

                    files.add(docMetadataStructure);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    files.sort((d1, d2) -> {
                        try {
                            Date date1 = sdf.parse(d1.getDocsData().getUploadDate());
                            Date date2 = sdf.parse(d2.getDocsData().getUploadDate());
                            if (date1 != null && date2 != null)
                                return date2.compareTo(date1); // Descending: newest first
                        } catch (ParseException e) {
                            Log.e("UPDATE_FILES_LIST_ERROR", "comparing upload date failed", e);
                            return 0; // fallback if parsing fails
                        }
                        return 0;
                    });
                    Log.d("FILES_DEBUG", "Total files loaded: " + files.size());

                    for (int i = 0; i < files.size(); i++) {
                        DocMetadataStructure doc = files.get(i);
                        String title = doc.getDocsData().getTitle();
                        String uploadDate = doc.getDocsData().getUploadDate();
                        Log.d("FILES_DEBUG", "File " + i + ": title=" + title + ", date=" + uploadDate);
                    }

                    documentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"cannot update list from db",Toast.LENGTH_SHORT).show();
            }
        });
        documentAdapter.notifyDataSetChanged();


    }
    private void uploadFileToDatabase(Uri fileUri, String title, String description){             //if upload success, load bottom sheet for title, description
        uploadFile(fileUri, title, description, new UploadCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(),"Upload successful",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                updateFilesList(uid);
            }

            @Override
            public String getExtension(Uri fileUri) {
                return getFileExtensionFromUri(requireContext(),fileUri);
            }

            @Override
            public void onStart() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) progress); // Only for horizontal bar
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),"Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static String getFileExtension(String path) {
        if (path == null) return "";
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < path.length() - 1) {
            return path.substring(dotIndex + 1); // exclude the dot
        }
        return "";
    }

}
