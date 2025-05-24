package com.example.change_things_android_final_demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class register_view extends AppCompatActivity {

    private EditText mEmail, mPassword, mUsrName;
    private Button mRegister, mUploadImage;
    private ImageView mImageView;

    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private Uri profileImageUri;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        mEmail = findViewById(R.id.editTextEmailAddress2);
        mPassword = findViewById(R.id.editregisterPassword);
        mRegister = findViewById(R.id.register);
        mUploadImage = findViewById(R.id.upload_image);
        mImageView = findViewById(R.id.imageview);
        mUsrName = findViewById(R.id.editTextName);

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        ActivityResultLauncher<String> imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageUri = uri;
                        Glide.with(this).load(uri).into(mImageView);
                    }
                });

        mUploadImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        mRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String usrName = mUsrName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || usrName.isEmpty()) {
            Toast.makeText(this, "請輸入信箱和密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageUri == null) {
            Toast.makeText(this, "請選擇頭像圖片", Toast.LENGTH_SHORT).show();
            return;
        }

        mRegister.setEnabled(false);
        Toast.makeText(this, "註冊中...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uploadImageToFirebase();
                    } else {
                        Toast.makeText(this, "註冊失敗: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        mRegister.setEnabled(true);
                    }
                });
    }

    private void uploadImageToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || profileImageUri == null) {
            mRegister.setEnabled(true);
            return;
        }

        String imagePath = "users/" + user.getUid() +
                "/profile_images/profile_" + System.currentTimeMillis() + ".jpg";

        StorageReference profileRef = storageRef.child(imagePath);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploader", user.getUid())
                .build();

        uploadTask = profileRef.putFile(profileImageUri, metadata);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserProfile(user, uri.toString(), imagePath);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "圖片上傳失敗: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mRegister.setEnabled(true);
                });
    }

    private void updateUserProfile(FirebaseUser user, String imageUrl, String imagePath) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mUsrName.getText().toString().trim())
                .setPhotoUri(Uri.parse(imageUrl))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserDataToDatabase(user.getUid(), imageUrl, imagePath);
                    } else {
                        Toast.makeText(this, "更新個人資料失敗", Toast.LENGTH_SHORT).show();
                        mRegister.setEnabled(true);
                    }
                });
    }

    private void saveUserDataToDatabase(String userId, String imageUrl, String imagePath) {
        DatabaseReference realtimeRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("profileImageUrl", imageUrl);
        userData.put("displayName", mUsrName.getText().toString().trim());
        userData.put("profileImagePath", imagePath);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLogin", System.currentTimeMillis());
        userData.put("discord", "");
        userData.put("line", "");
        userData.put("instagram", "");

        realtimeRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "註冊成功！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, login_view.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "資料儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mRegister.setEnabled(true);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadTask != null && uploadTask.isInProgress()) {
            uploadTask.cancel();
        }
    }
}
