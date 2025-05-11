package com.example.change_things_android_final_demo.ui.slideshow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.change_things_android_final_demo.R;
import com.example.change_things_android_final_demo.databinding.FragmentSlideshowBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class SlideshowFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FragmentSlideshowBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Map Fragment not found in layout!", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationFeatures();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        loadItemsFromFirebase();
    }

    private void enableLocationFeatures() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("My Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationFeatures();
        } else {
            Toast.makeText(getContext(), "Location permission is required to show your location",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void loadItemsFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Images"); // ✅ 修正路徑

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) { // 使用者 UID
                    for (DataSnapshot dataSnapshot : userSnapshot.getChildren()) { // 商品項目
                        String name = dataSnapshot.child("caption").getValue(String.class);
                        String price = dataSnapshot.child("itemprice").getValue(String.class);
                        String exchange = dataSnapshot.child("itemchange").getValue(String.class);
                        Double lat = dataSnapshot.child("latitude").getValue(Double.class);
                        Double lon = dataSnapshot.child("longitude").getValue(Double.class);
                        String image = dataSnapshot.child("imageURL").getValue(String.class);

                        // 防止空值 crash
                        if (name == null || price == null || exchange == null || lat == null || lon == null || image == null)
                            continue;

                        LatLng itemLocation = new LatLng(lat, lon);

                        Glide.with(SlideshowFragment.this)
                                .asBitmap()
                                .load(image)
                                .into(new CustomTarget<Bitmap>(100, 100) {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        // 裁切圓形
                                        int size = Math.min(resource.getWidth(), resource.getHeight());
                                        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(output);

                                        final Paint paint = new Paint();
                                        final Rect rect = new Rect(0, 0, size, size);
                                        final RectF rectF = new RectF(rect);

                                        paint.setAntiAlias(true);
                                        canvas.drawARGB(0, 0, 0, 0);
                                        canvas.drawOval(rectF, paint);
                                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                                        canvas.drawBitmap(resource, null, rect, paint);

                                        mMap.addMarker(new MarkerOptions()
                                                .position(itemLocation)
                                                .title(name)
                                                .snippet("希望交換物:" + exchange + "\n價格:" + price)
                                                .icon(BitmapDescriptorFactory.fromBitmap(output)));
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                                });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "讀取失敗: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                return null;
            }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.google_map_onclick, null);
                TextView titleTextView = view.findViewById(R.id.titleTextView);
                TextView snippetTextView = view.findViewById(R.id.snippetTextView);
                titleTextView.setText(marker.getTitle());
                snippetTextView.setText(marker.getSnippet());
                return view;
            }
        });
    }
}

