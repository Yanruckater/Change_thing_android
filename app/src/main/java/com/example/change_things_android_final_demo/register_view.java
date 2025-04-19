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

    private static final String TAG = "RegisterView"; // 用於 Logcat 標籤

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
                        Log.d(TAG, "圖片選擇成功: " + uri.toString());
                    } else {
                        Log.d(TAG, "未選擇圖片");
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
            Log.e(TAG, "輸入驗證失敗: 信箱或密碼為空");
            return;
        }

        if (profileImageUri == null) {
            Toast.makeText(this, "請選擇頭像圖片", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "輸入驗證失敗: 未選擇頭像圖片");
            return;
        }

        // 禁用按鈕防止重複提交
        mRegister.setEnabled(false);
        Toast.makeText(this, "註冊中...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "開始註冊流程，信箱: " + email);

        // 創建用戶帳號
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "身份驗證成功，UID: " + mAuth.getCurrentUser().getUid());
                        uploadImageToFirebase();
                        startActivity(new Intent(this, login_view.class));
                        Toast.makeText(this, "註冊成功: " , Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "未知錯誤";
                        Toast.makeText(this, "註冊失敗: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "身份驗證失敗: " + errorMsg);
                        mRegister.setEnabled(true);
                    }
                });
    }

    private void uploadImageToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || profileImageUri == null) {
            Toast.makeText(this, "用戶未登錄或未選擇圖片", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "圖片上傳失敗: 用戶未登錄或未選擇圖片");
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
        Log.d(TAG, "開始上傳圖片: " + imagePath);
        uploadTask = profileRef.putFile(profileImageUri, metadata);
        uploadTask.addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "圖片上傳進度: " + progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "圖片上傳成功");
                    // 獲取下載URL
                    profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "獲取下載URL成功: " + uri.toString());
                        updateUserProfile(user, uri.toString(), imagePath);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "獲取圖片URL失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "獲取圖片URL失敗: " + e.getMessage());
                        mRegister.setEnabled(true);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "圖片上傳失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "圖片上傳失敗: " + e.getMessage());
                    mRegister.setEnabled(true);
                });
    }

    private void updateUserProfile(FirebaseUser user, String imageUrl, String imagePath) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getEmail().split("@")[0]) // 使用信箱前綴作為預設名稱
                .setPhotoUri(Uri.parse(imageUrl))
                .build();

        Log.d(TAG, "開始更新用戶個人資料，顯示名稱: " + user.getEmail().split("@")[0] + ", 圖片URL: " + imageUrl);
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "個人資料更新成功");
                        saveUserDataToFirestore(user.getUid(), imageUrl, imagePath);
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "未知錯誤";
                        Toast.makeText(this, "更新個人資料失敗: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "個人資料更新失敗: " + errorMsg);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消進行中的上傳任務
        if (uploadTask != null && uploadTask.isInProgress()) {
            uploadTask.cancel();
            Log.d(TAG, "活動銷毀，取消圖片上傳任務");
        }
    }
}