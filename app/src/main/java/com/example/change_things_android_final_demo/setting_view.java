package com.example.change_things_android_final_demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class setting_view extends Fragment {

    private ImageView imageProfile;
    private EditText editDisplayName;
    private Uri selectedImageUri;
    private FirebaseUser user;
    private StorageReference storageRef;
    private AlertDialog loadingDialog; // loading

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting_view, container, false);

        imageProfile = view.findViewById(R.id.imageProfile);
        editDisplayName = view.findViewById(R.id.editDisplayName);
        Button btnChangeImage = view.findViewById(R.id.btnChangeImage);
        Button btnSave = view.findViewById(R.id.btnSave);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show();
            return view;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("profile_images");

        StorageReference fileRef = storageRef.child("profile.jpg");  // 可以固定用 profile.jpg

        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(imageProfile);
        }

        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        btnSave.setOnClickListener(v -> saveChanges());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageProfile.setImageURI(selectedImageUri);
        }
    }

    private void saveChanges() {
        String displayName = editDisplayName.getText().toString().trim();
        if (user == null || !isAdded()) return;

        showLoadingDialog();

        if (selectedImageUri != null) {
            StorageReference fileRef = storageRef.child("profile.jpg");

            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .setDisplayName(displayName)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.reload().addOnCompleteListener(reloadTask -> {
                                    dismissLoadingDialog();
                                    if (reloadTask.isSuccessful() && isAdded()) {
                                        Toast.makeText(requireContext(), "更新成功", Toast.LENGTH_SHORT).show();
                                        Glide.with(requireContext())
                                                .load(user.getPhotoUrl())
                                                .into(imageProfile);

                                        if(getActivity() instanceof Navigation_drawer_view){
                                            ((Navigation_drawer_view) getActivity()).updateNavigationHeader();
                                        }
                                    }
                                });
                            }
                        });
                    }))
                    .addOnFailureListener(e -> {
                        dismissLoadingDialog();
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "上傳失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 如果沒有選照片，只更新名字
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                dismissLoadingDialog();
                if (task.isSuccessful() && isAdded()) {
                    Toast.makeText(requireContext(), "只更新了名稱", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoadingDialog() {
        if (!isAdded() || loadingDialog != null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.loading_view, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && isAdded()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}

