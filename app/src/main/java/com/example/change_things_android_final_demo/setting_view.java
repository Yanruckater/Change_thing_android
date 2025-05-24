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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class setting_view extends Fragment {

    private ImageView imageProfile;
    private EditText editDisplayName, dcard, line, instagram;
    private Uri selectedImageUri;
    private FirebaseUser user;
    private StorageReference storageRef;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting_view, container, false);

        imageProfile = view.findViewById(R.id.imageProfile);
        editDisplayName = view.findViewById(R.id.editDisplayName);
        dcard = view.findViewById(R.id.editDiscord);
        line = view.findViewById(R.id.editLine);
        instagram = view.findViewById(R.id.editInstagram);
        Button btnChangeImage = view.findViewById(R.id.btnChangeImage);
        Button btnSave = view.findViewById(R.id.btnSave);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show();
            return view;
        }

        storageRef = FirebaseStorage.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("profile_images");

        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(imageProfile);
        }

        loadUserDataFromRealtimeDB();

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

    private void loadUserDataFromRealtimeDB() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(user.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayNameValue = snapshot.child("displayName").getValue(String.class);
                    String dcardValue = snapshot.child("discord").getValue(String.class);
                    String lineValue = snapshot.child("line").getValue(String.class);
                    String instagramValue = snapshot.child("instagram").getValue(String.class);

                    if (displayNameValue != null) editDisplayName.setText(displayNameValue);
                    if (dcardValue != null) dcard.setText(dcardValue);
                    if (lineValue != null) line.setText(lineValue);
                    if (instagramValue != null) instagram.setText(instagramValue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "載入資料失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        String displayName = editDisplayName.getText().toString().trim();
        String dcardValue = dcard.getText().toString().trim();
        String lineValue = line.getText().toString().trim();
        String instagramValue = instagram.getText().toString().trim();

        if (user == null || !isAdded()) return;

        showLoadingDialog();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(user.getUid());

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
                                userRef.child("displayName").setValue(displayName);
                                userRef.child("dcard").setValue(dcardValue);
                                userRef.child("line").setValue(lineValue);
                                userRef.child("instagram").setValue(instagramValue);

                                dismissLoadingDialog();
                                Toast.makeText(requireContext(), "更新成功", Toast.LENGTH_SHORT).show();

                                if (getActivity() instanceof Navigation_drawer_view) {
                                    ((Navigation_drawer_view) getActivity()).updateNavigationHeader();
                                }
                            }
                        });
                    }))
                    .addOnFailureListener(e -> {
                        dismissLoadingDialog();
                        Toast.makeText(requireContext(), "上傳失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 只更新資料
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                dismissLoadingDialog();
                if (task.isSuccessful()) {
                    userRef.child("displayName").setValue(displayName);
                    userRef.child("dcard").setValue(dcardValue);
                    userRef.child("line").setValue(lineValue);
                    userRef.child("instagram").setValue(instagramValue);

                    Toast.makeText(requireContext(), "更新成功（不含照片）", Toast.LENGTH_SHORT).show();
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
