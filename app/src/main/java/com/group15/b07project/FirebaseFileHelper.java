package com.group15.b07project;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

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
        default String getExtension(Uri fileUri) {
            return "";
        }

        default void onStart() {
        }

        void onSuccess();

        default void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
        }

        void onFailure(Exception e);
    }

    public static void uploadFile(Uri fileUri, String title, String description, UploadCallback callback) {
        if (fileUri == null || title == null) {
            if (callback != null)
                callback.onFailure(new IllegalArgumentException("Invalid arguments"));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String uid = user.getUid();

        String fileId = UUID.randomUUID().toString();
        String extension = callback.getExtension(fileUri);
        Log.d("UPLOAD_DEBUG", "Extracted extension: " + extension);
        String storagePath = "Documents" +
                "/" + fileId + (extension != null ? "." + extension : "");
        Log.d("UPLOAD_DEBUG", "Final storage path: " + storagePath);

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(storagePath);

        UploadTask uploadTask = fileRef.putFile(fileUri);
        callback.onStart();
        uploadTask.addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            saveFileMetadata(uid, fileId, title, description, downloadUri.toString(), storagePath);
            callback.onSuccess();
        })).addOnProgressListener(callback::onProgress).addOnFailureListener(e -> {
            callback.onFailure(e);
            Log.e("UPLOAD_ERROR", "Upload failed: " + e.getMessage(), e);
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

    public static void editFileMetadata(String uid, String fileId, String title, String description) {
        DatabaseReference docRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("Documents").child(fileId);
        docRef.child("title").setValue(title);
        docRef.child("description").setValue(description);
    }

    public static String getFileExtensionFromUri(Context context, Uri uri) {
        if (uri == null) return "";

        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType == null) return "";

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(mimeType);

        Log.d("UPLOAD_DEBUG", "MIME type: " + mimeType + ", extension: " + extension);

        return extension != null ? extension : "";
    }

    public static void deleteFile(String userId, String uploadId, String storagePath, UploadCallback callback) {
        StorageReference fileRef = FirebaseStorage.getInstance().getReference(storagePath);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Documents").child(uploadId);

        fileRef.delete().addOnSuccessListener(aVoid -> {
            dbRef.removeValue()
                    .addOnSuccessListener(unused -> callback.onSuccess())
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }
}

