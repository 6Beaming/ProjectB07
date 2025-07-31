package com.group15.b07project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DocumentsToPackFragment extends Fragment {

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents_to_pack,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filePickerLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if (result.getResultCode()== MainActivity.RESULT_OK && result.getData()!=null){
                        Uri fileUri=result.getData().getData();
                        if (fileUri!=null){
                            uploadFileToDatabase(fileUri);
                        }
                    }
                }
        );
    }


    private void uploadFileToDatabase(Uri fileUri){
        uploadFile(fileUri, "uploads", new UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public static void uploadFile(Uri fileUri, String folderName, UploadCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storageRef.child(folderName + "/" + fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(task -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            callback.onSuccess(url);
                        }))
                .addOnFailureListener(callback::onFailure);
    }

    public static void saveFileMetadata(String url, String fileName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("fileUrl", url);
        data.put("fileName", fileName);
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("documents").add(data);
    }
}
