package com.example.change_things_android_final_demo;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UploadItemActivity extends AppCompatActivity {

    private ImageView itemImageView;
    private TextInputEditText itemNameEditText, itemDescEditText;
    private TextView locationTextView;
    private MaterialButton uploadButton;
    private Uri imageUri;
    private double latitude, longitude;

    /*private FusedLocationProviderClient locationClient;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;*/

    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        itemImageView = findViewById(R.id.itemImageView);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemDescEditText = findViewById(R.id.itemDescEditText);
        locationTextView = findViewById(R.id.locationTextView);
        uploadButton = findViewById(R.id.uploadButton);


    }
}