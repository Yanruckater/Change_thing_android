package com.example.change_things_android_final_demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.change_things_android_final_demo.Dataupload.DataClass;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UploadItemActivity extends AppCompatActivity {

    //firebase
    private ImageView SelectPhoto;
    private Bitmap bitmap;
    private TextInputEditText itemNameEditText, itemDescEditText, itemPriceEditText, itemHopeChangeEditText;
    private MaterialButton uploadButton;
    private Uri imageUri;
    private AlertDialog loadingDialog;

    //初始化firebase
    private DatabaseReference mDatabase;
    private FirebaseStorage mFirebaseStrong;
    private StorageReference mStorageRef;

    //loaction
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final long UPDATE_INTERVAL = 10000;  // 10秒
    private static final long FASTEST_INTERVAL = 5000;  // 5秒
    private static final int MAX_GEOCODE_RESULTS = 1;

    // location UI components
    private TextView locationTextView;

    // Location related
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Geocoder geocoder;
    private boolean isGeocoderAvailable;
    private double latitude = 0.0;
    private double longitude = 0.0;

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

        //初始化Firebase
        FirebaseApp.initializeApp(this);
        mFirebaseStrong = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStrong.getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference("Images").child(uid);
            Log.d("FirebaseAuth", "使用者已登入：" + user.getEmail());
        } else {
            Log.d("FirebaseAuth", "尚未登入！");
            Toast.makeText(this, "請先登入帳號", Toast.LENGTH_SHORT).show();
            finish(); // 強制關閉這個 activity，避免 NullPointer
            return;
        }

        // 初始化位置服务
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        isGeocoderAvailable = Geocoder.isPresent();

        createLocationRequest();
        setupLocationCallback();
        initViews();
        checkLocationPermission();


        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK){
                            Intent data = result.getData();
                            imageUri = data.getData();
                            SelectPhoto.setImageURI(imageUri);
                        }else{
                            Toast.makeText(UploadItemActivity.this, "未選擇圖片", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        SelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photopick = new Intent();
                photopick.setAction(Intent.ACTION_GET_CONTENT);
                photopick.setType("image/*");
                activityResultLauncher.launch(photopick);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadTofireBase(imageUri);
                } else {
                    Toast.makeText(UploadItemActivity.this, "未選擇圖片", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void  uploadTofireBase(Uri imageUri) {
        String itemName = itemNameEditText.getText().toString();
        String itemDesc = itemDescEditText.getText().toString();
        String itemPrice = itemPriceEditText.getText().toString();
        String itemHopeChange = itemHopeChangeEditText.getText().toString();


        if(itemName.isEmpty() || itemDesc.isEmpty() || itemPrice.isEmpty() || itemHopeChange.isEmpty()){
            Toast.makeText(this, "請輸入物品名稱、描述、欲交換物和價錢", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)   return;

        String userName = user.getDisplayName();
        String userImage = user.getPhotoUrl()!= null ? user.getPhotoUrl().toString() : "";

        String fileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imageReference = mStorageRef.child("item_picture").child(uid).child("item_images").child(fileName);

        imageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("imageURL", uri.toString());
                    dataMap.put("caption", itemName);
                    dataMap.put("itemchange", itemHopeChange);
                    dataMap.put("itemprice", itemPrice);
                    dataMap.put("latitude", latitude);
                    dataMap.put("longitude", longitude);
                    dataMap.put("location", locationTextView.getText().toString());
                    dataMap.put("userName", userName);
                    dataMap.put("userImage", userImage);

                    String key = mDatabase.push().getKey();
                    if (key != null) {
                        mDatabase.child(key).setValue(dataMap);
                        Toast.makeText(UploadItemActivity.this, "上傳成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadItemActivity.this, Navigation_drawer_view.class);
                        intent.putExtra("target_fragment","gallery");
                        startActivity(intent);
                        finish();
                    }
                }))
                .addOnProgressListener(snapshot -> showLoadingDialog())
                .addOnFailureListener(e -> Toast.makeText(UploadItemActivity.this, "上傳失敗", Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> dismissLoadingDialog());
    }

    private String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }

    private void initViews() {
        SelectPhoto = findViewById(R.id.itemImageView);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemDescEditText = findViewById(R.id.itemDescEditText);
        itemPriceEditText = findViewById(R.id.itemPriceEditText);
        locationTextView = findViewById(R.id.locationTextView);
        itemHopeChangeEditText = findViewById(R.id.itemExchangeEditText);
        uploadButton = findViewById(R.id.uploadButton);
    }


    //loading彈出視窗
    private void showLoadingDialog() {
        if (isFinishing() || isDestroyed()) return; // 避免已結束還呼叫
        if (loadingDialog != null && loadingDialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.loading_view, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            if (!isFinishing() && !isDestroyed()) {
                loadingDialog.dismiss();
            }
            loadingDialog = null;
        }
    }


    //位置請求
    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();
    }

    //位置更新時的邏輯
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        updateLocationText();
                    }
                }
            }
        };
    }

    //是否取得定位功能
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
            getLastLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //持續接收位置更新
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    //取得裝置最後的定位
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        updateLocationText();
                    } else {
                        locationTextView.setText("暫時無法取得位置");
                    }
                })
                .addOnFailureListener(e -> {
                    locationTextView.setText("定位失敗");
                    Toast.makeText(this, "位置獲取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //根據目前的經緯度，更新畫面上顯示的位置資訊
    private void updateLocationText() {
        runOnUiThread(() -> {
            if (latitude != 0.0 && longitude != 0.0) {
                if (isGeocoderAvailable) {
                    fetchAddressFromLocation(latitude, longitude);
                } else {
                    showCoordinatesFallback();
                }
            } else {
                locationTextView.setText("定位中...");
            }
        });
    }

    //地址文字
    private void fetchAddressFromLocation(double lat, double lng) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, MAX_GEOCODE_RESULTS);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = formatAddress(address);
                    runOnUiThread(() -> locationTextView.setText(addressText));
                } else {
                    showCoordinatesFallback();
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    showCoordinatesFallback();
                    Toast.makeText(this, "地址解析失敗，請檢查網路連接", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            sb.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    private void showCoordinatesFallback() {
        String coordinateText = String.format("座標: %.4f, %.4f", latitude, longitude);
        locationTextView.setText(coordinateText);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
                getLastLocation();
            } else {
                locationTextView.setText("需要位置權限");
                Toast.makeText(this, "位置功能已禁用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}