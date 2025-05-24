package com.example.change_things_android_final_demo.recyclerfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.change_things_android_final_demo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Editltem extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editName,editexchange,editPrice;
    private Spinner meditSpinner;
    private Button saveButton;
    private String itemkey;
    private ImageView mitemImage;
    private Uri imageUri;

    public Editltem(){}

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            mitemImage.setImageURI(imageUri);
        }
    }


    @Nullable
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_editltem, container, false);

        editName = view.findViewById(R.id.editName);
        editexchange = view.findViewById(R.id.editExchangeItem);
        editPrice = view.findViewById(R.id.editPrice);
        meditSpinner = view.findViewById(R.id.editStatus);
        saveButton = view.findViewById(R.id.saveButton);
        mitemImage = view.findViewById(R.id.itemImageView2);

        mitemImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "選擇圖片"), PICK_IMAGE_REQUEST);
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            requireContext(),
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meditSpinner.setAdapter(adapter);

        Bundle args = getArguments();
        if(args != null) {
            // 取得資料
            itemkey = args.getString("itemkey");
            editName.setText(args.getString("name"));
            editexchange.setText(args.getString("exchangeItem"));
            editPrice.setText(args.getString("price"));
            String imageUri = args.getString("image");
            Glide.with(this).load(imageUri).placeholder(R.drawable.baseline_photo_size_select_actual_24).into(mitemImage);



            String status = args.getString("status").replace("狀態：","");
            if(status.equals("出租中")){
                meditSpinner.setSelection(0);
            }else {
                meditSpinner.setSelection(1);
            }
        }

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString();
            String exchangeItem = editexchange.getText().toString();
            String price = editPrice.getText().toString();
            String status = meditSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(exchangeItem) || TextUtils.isEmpty(price)) {
                Toast.makeText(getContext(), "請輸入完整資料", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Images").child(uid).child(itemkey);

            if (imageUri != null && imageUri.toString().startsWith("content://")) {
                // 使用者選了新的圖片 -> 上傳 Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference("item_picture/" + uid + "/item_images/" + System.currentTimeMillis() + ".png");

                storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            //拿到下載連結後更新 Firebase DB
                            ref.child("caption").setValue(name);
                            ref.child("itemchange").setValue(exchangeItem);
                            ref.child("itemprice").setValue(price);
                            ref.child("status").setValue(status);
                            ref.child("imageURL").setValue(downloadUri.toString());

                            Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        })
                ).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "圖片上傳失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else{
                ref.child("caption").setValue(name);
                ref.child("itemchange").setValue(exchangeItem);
                ref.child("itemprice").setValue(price);
                ref.child("status").setValue(status);

                Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });



        return view;
    }
}