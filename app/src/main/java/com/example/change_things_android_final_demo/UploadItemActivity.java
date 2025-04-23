package com.example.change_things_android_final_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UploadItemActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final long UPDATE_INTERVAL = 10000;  // 10秒
    private static final long FASTEST_INTERVAL = 5000;  // 5秒
    private static final int MAX_GEOCODE_RESULTS = 1;

    // UI components
    private ImageView itemImageView;
    private TextInputEditText itemNameEditText, itemDescEditText;
    private TextView locationTextView;
    private MaterialButton uploadButton;

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

        // 初始化位置服务
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        isGeocoderAvailable = Geocoder.isPresent();

        createLocationRequest();
        setupLocationCallback();
        initViews();
        checkLocationPermission();
    }

    //基本功
    private void initViews() {
        itemImageView = findViewById(R.id.itemImageView);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemDescEditText = findViewById(R.id.itemDescEditText);
        locationTextView = findViewById(R.id.locationTextView);
        uploadButton = findViewById(R.id.uploadButton);
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