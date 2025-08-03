package com.group15.b07project;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.group15.b07project.FirebaseFileHelper.*;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group15.b07project.FirebaseFileHelper.*;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentsToPackFragment extends Fragment implements DocumentAdapter.OnFileItemClickListener {
    private RecyclerView recyclerView_docs;
    private List<DocMetadataStructure> files;
    private ImageButton back_to_emergency_info;
    private FloatingActionButton fab_add_file;
    private ProgressBar progressBar;
//    private long downloadId;
//    private BroadcastReceiver onDownloadComplete;
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

        uid="123";
//        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
//        if (user!=null) uid=user.getUid();

        recyclerView_docs=view.findViewById(R.id.recyclerview_docs);
        fab_add_file=view.findViewById(R.id.fab_add_docs);
        back_to_emergency_info=view.findViewById(R.id.button_back_to_emegencyInfo);
        progressBar=view.findViewById(R.id.upload_progress_bar);

        recyclerView_docs.setLayoutManager(new LinearLayoutManager(getContext()));
        files= new ArrayList<>();
        documentAdapter=new DocumentAdapter(files,this);
        recyclerView_docs.setAdapter(documentAdapter);

        updateFilesList(uid);

        filePickerLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if (result.getResultCode()== MainActivity.RESULT_OK && result.getData()!=null){
                        Toast.makeText(getContext(),"File selected",Toast.LENGTH_SHORT).show();
                        Uri fileUri=result.getData().getData();
                        if (fileUri!=null){
                                        //ask for title and description
                            getChildFragmentManager().setFragmentResultListener(
                                    "metadata_request_key",
                                    this,
                                    new FragmentResultListener() {
                                        @Override
                                        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                                            String title = result.getString("title");
                                            String description = result.getString("description");
                                            uploadFileToDatabase(fileUri, title,description);
                                        }
                                    }
                            );
                            showMetadataBottomSheet(null,null);
                            //start uploading
                        }
                    }
                }
        );
        fab_add_file.setOnClickListener(new View.OnClickListener() {    //user choose file, then set title, description
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                filePickerLauncher.launch(intent);
            }
        });
        back_to_emergency_info.setOnClickListener(v->loadFragment(new StorageOfEmergencyInfoFragment()));
//        onDownloadComplete = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                if (id == downloadId) {
//                    Toast.makeText(context, "Download complete!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//        ContextCompat.registerReceiver(requireContext(), onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED);
    }
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        requireContext().unregisterReceiver(onDownloadComplete);
//    }


    @Override
    public void onDownloadClick(int position) {
        String url=files.get(position).getDocsdata().getDownloadUrl();
        String title=files.get(position).getDocsdata().getTitle()+getFileExtension(files.get(position).getDocsdata().getStoragePath());
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
        String title_old=files.get(position).getDocsdata().getTitle();
        String description_old=files.get(position).getDocsdata().getDescription();
        getChildFragmentManager().setFragmentResultListener(
                "metadata_request_key",
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        String title = result.getString("title");
                        String description = result.getString("description");
                        editFileMetadata(uid,fileId,title,description);
                        updateFilesList(uid);
                    }
                }
        );
        showMetadataBottomSheet(title_old,description_old);
    }

    @Override
    public void onDeleteClick(int position) {
        String fileId=files.get(position).getDocId();
        String storagePath=files.get(position).getDocsdata().getStoragePath();
        String title_time=files.get(position).getDocsdata().getTitle()+files.get(position).getDocsdata().getUploadDate();
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

    private void showMetadataBottomSheet(String title,String description) {        //load sheet for adding file
        EditDocumentFragment sheet = EditDocumentFragment.newInstance(title, description);
        sheet.show(getChildFragmentManager(), "docSheet");
    }

    private void updateFilesList(String uid){
        files.clear();

        DatabaseReference docsref=FirebaseDatabase.getInstance().getReference("users").child(uid).child("Documents");
        docsref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot docsnapshot:
                     snapshot.getChildren()) {
                    Log.d("FILES_DEBUG", "Found docId: " + docsnapshot.getKey());
                    String docId=docsnapshot.getKey();
                    DocsdataStructure docsdataStructure=docsnapshot.getValue(DocsdataStructure.class);
                    DocMetadataStructure docMetadataStructure=new DocMetadataStructure(docId,docsdataStructure);
//                    StorageReference storageReference=FirebaseStorage.getInstance().getReference(docsdataStructure.getStoragePath());
//                    storageReference.getDownloadUrl().addOnSuccessListener(v-> {
//                        files.add(docMetadataStructure);
//                    }).addOnFailureListener(v->{
//                            Log.v("updateList missing node","remove the node");
//                            docsref.child(docId).removeValue();}
//                    );

                    files.add(docMetadataStructure);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Collections.sort(files, new Comparator<DocMetadataStructure>() {
                        @Override
                        public int compare(DocMetadataStructure d1, DocMetadataStructure d2) {
                            try {
                                Date date1 = sdf.parse(d1.getDocsdata().getUploadDate());
                                Date date2 = sdf.parse(d2.getDocsdata().getUploadDate());
                                return date2.compareTo(date1); // Descending: newest first
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0; // fallback if parsing fails
                            }
                        }
                    });
                    Log.d("FILES_DEBUG", "Total files loaded: " + files.size());

                    for (int i = 0; i < files.size(); i++) {
                        DocMetadataStructure doc = files.get(i);
                        String title = doc.getDocsdata().getTitle();
                        String uploadDate = doc.getDocsdata().getUploadDate();
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
