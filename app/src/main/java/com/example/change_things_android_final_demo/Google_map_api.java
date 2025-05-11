    package com.example.change_things_android_final_demo;

    import androidx.annotation.NonNull;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.FragmentActivity;

    import android.Manifest;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.bumptech.glide.Glide;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.example.change_things_android_final_demo.databinding.ActivityGoogleMapApiBinding;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;

    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import android.graphics.Bitmap;
    import android.graphics.drawable.Drawable;
    import androidx.annotation.Nullable;
    import com.bumptech.glide.request.target.CustomTarget;
    import com.bumptech.glide.request.transition.Transition;


    public class Google_map_api extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private ActivityGoogleMapApiBinding binding;
        private FusedLocationProviderClient fusedLocationClient;
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            binding = ActivityGoogleMapApiBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            // 檢查位置權限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // 已經有權限，啟用定位功能
                enableLocationFeatures();
            } else {
                // 請求權限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
            loadItemsFromFirebase();
        }
        private void enableLocationFeatures() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // 啟用地圖上的我的位置按鈕
                mMap.setMyLocationEnabled(true);

                // 獲取最後已知位置
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    // 移動鏡頭到使用者位置
                                    LatLng currentLocation = new LatLng(
                                            location.getLatitude(),
                                            location.getLongitude());
                                    mMap.addMarker(new MarkerOptions()
                                            .position(currentLocation)
                                            .title("My Location"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            currentLocation, 15));
                                }
                            }
                        });
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 權限被授予
                    enableLocationFeatures();
                } else {
                    // 權限被拒絕
                    Toast.makeText(this, "Location permission is required to show your location",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void loadItemsFromFirebase(){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Image");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String name = dataSnapshot.child("caption").getValue(String.class);
                        String price = dataSnapshot.child("itemprice").getValue(String.class);
                        String exchange = dataSnapshot.child("itemchange").getValue(String.class);
                        double lat = dataSnapshot.child("latitude").getValue(Double.class);
                        double lon = dataSnapshot.child("longitude").getValue(Double.class);
                        String image = dataSnapshot.child("imageURL").getValue(String.class);

                        LatLng itemLocation = new LatLng(lat, lon);

                        Glide.with(Google_map_api.this).asBitmap().load(image).into(new CustomTarget<Bitmap>(200, 200) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Marker marker = mMap.addMarker(new MarkerOptions().position(itemLocation).title(name).snippet("希望交換物:" + exchange + "\n價格:" + price).icon(BitmapDescriptorFactory.fromBitmap(resource)));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Google_map_api.this,"讀取失敗:" + error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Nullable
                @Override
                public View getInfoContents(@NonNull Marker marker) {
                    return null; // 使用系統預設外框
                }

                @Nullable
                @Override
                public View getInfoWindow(@NonNull Marker marker) {
                    View view = getLayoutInflater().inflate(R.layout.google_map_onclick,null);

                    TextView titleTextView = view.findViewById(R.id.titleTextView);
                    TextView snippetTextView = view.findViewById(R.id.snippetTextView);

                    titleTextView.setText(marker.getTitle());
                    snippetTextView.setText(marker.getSnippet());

                    return view;
                }
            });
        }

    }