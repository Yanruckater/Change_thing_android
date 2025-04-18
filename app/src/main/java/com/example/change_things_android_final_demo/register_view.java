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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class register_view extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mRegister, mUploadImage;
    private ImageView mImageView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private StorageReference storageRef;
    private Uri profileImageUri;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_view);

        // 初始化UI元件
        mEmail = findViewById(R.id.editTextEmailAddress2);
        mPassword = findViewById(R.id.editregisterPassword);
        mRegister = findViewById(R.id.register);
        mUploadImage = findViewById(R.id.upload_image);
        mImageView = findViewById(R.id.imageview);

        // 初始化Firebase服務
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // 圖片選擇器
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

        // 輸入驗證
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "請輸入信箱和密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profileImageUri == null) {
            Toast.makeText(this, "請選擇頭像圖片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 禁用按鈕防止重複提交
        mRegister.setEnabled(false);
        Toast.makeText(this, "註冊中...", Toast.LENGTH_SHORT).show();

        // 創建用戶帳號
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

        // 結構化儲存路徑：users/{uid}/profile_images/profile_{timestamp}.jpg
        String imagePath = "users/" + user.getUid() +
                "/profile_images/profile_" + System.currentTimeMillis() + ".jpg";

        StorageReference profileRef = storageRef.child(imagePath);

        // 設定檔案中繼資料
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploader", user.getUid())
                .build();

        // 執行上傳
        uploadTask = profileRef.putFile(profileImageUri, metadata);
        uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    Log.d("Upload", "進度: " + progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // 獲取下載URL
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateUserProfile(user, uri.toString(), imagePath);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "圖片上傳失敗: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mRegister.setEnabled(true);
                });
    }

    private void updateUserProfile(FirebaseUser user, String imageUrl, String imagePath) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getEmail().split("@")[0]) // 使用信箱前綴作為預設名稱
                .setPhotoUri(Uri.parse(imageUrl))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserDataToFirestore(user.getUid(), imageUrl, imagePath);
                    } else {
                        Toast.makeText(this, "更新個人資料失敗", Toast.LENGTH_SHORT).show();
                        mRegister.setEnabled(true);
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String imageUrl, String imagePath) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("profileImageUrl", imageUrl);
        userData.put("profileImagePath", imagePath);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("lastLogin", FieldValue.serverTimestamp());

        mFirestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "註冊成功！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, login_view.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "資料儲存失敗: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    mRegister.setEnabled(true);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消進行中的上傳任務
        if (uploadTask != null && uploadTask.isInProgress()) {
            uploadTask.cancel();
        }
    }
}