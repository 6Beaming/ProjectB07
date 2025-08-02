package com.group15.b07project;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class FirebaseFileHelper {

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void uploadFile(Uri fileUri, String title, String description, UploadCallback callback) {
        if (fileUri == null || title == null) {
            if (callback != null)
                callback.onFailure(new IllegalArgumentException("Invalid arguments"));
            return;
        }

//        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
//        if (user==null){
//            return;
//        }
//        String uid=user.getUid();
        String uid="123";          //dummy test

        String fileId = UUID.randomUUID().toString();
        String extension = getFileExtension(fileUri.getPath());
        String storagePath = "Documents" +
                "/" + fileId + (extension != null ? "." + extension : "");

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(storagePath);
        UploadTask uploadTask = fileRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            saveFileMetadata(uid, fileId, title, description, downloadUri.toString(), storagePath);
            if (callback != null) callback.onSuccess();
        })).addOnFailureListener(e -> {
            if (callback != null) callback.onFailure(e);
        });
    }

    private static void saveFileMetadata(String uid, String fileId, String title, String description, String downloadUrl, String storagePath) {

        DatabaseReference docRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("Documents").child(fileId);

        String uploadDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("description", description);
        metadata.put("uploadDate", uploadDate);
        metadata.put("downloadUrl", downloadUrl);
        metadata.put("storagePath", storagePath);

        docRef.setValue(metadata);
    }

    private static String getFileExtension(String path) {
        if (path == null) return null;
        int dot = path.lastIndexOf('.');
        return (dot >= 0 && dot < path.length() - 1) ? path.substring(dot + 1) : null;
    }

    public static void deleteFile(String userId, String uploadId, String storagePath, UploadCallback callback) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReference(storagePath);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("uploads").child(userId).child(uploadId);

        fileRef.delete().addOnSuccessListener(aVoid -> {
            dbRef.removeValue()
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }
}

