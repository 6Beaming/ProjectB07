package com.group15.b07project;

import static com.group15.b07project.FirebaseFileHelper.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DocumentsToPackFragment extends Fragment{
    private RecyclerView recyclerView_docs;
    private List<String> files;
    private FloatingActionButton fab_add_file;
    private DocumentAdapter documentAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents_to_pack,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView_docs=view.findViewById(R.id.recyclerview_docs);
        fab_add_file=view.findViewById(R.id.fab_add_docs);

        files= new ArrayList<>();
        documentAdapter=new DocumentAdapter(files);
        recyclerView_docs.setAdapter(documentAdapter);

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
                            showMetadataBottomSheet();
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

    }


    private void showMetadataBottomSheet() {
        EditDocumentFragment sheet = EditDocumentFragment.newInstance(null, null);
        sheet.show(getChildFragmentManager(), "docSheet");
    }

    private void uploadFileToDatabase(Uri fileUri, String title, String description){             //if upload success, load bottom sheet for title, description
        uploadFile(fileUri, title, description, new UploadCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(),"Upload successful",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),"Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
