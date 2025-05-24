package com.example.change_things_android_final_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.change_things_android_final_demo.recyclerfile.MyAdapter;
import com.example.change_things_android_final_demo.recyclerfile.itme_recycler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class recycler_view extends AppCompatActivity {

    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recycler_view);

        fab = findViewById(R.id.fab);

        drawerLayout = findViewById(R.id.drawer_layout);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<itme_recycler> items = new ArrayList<>();
        MyAdapter adapter = new MyAdapter(getApplicationContext(),items,"gallery");
        recyclerView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear(); // 清空列表
                for(DataSnapshot userdataSnapuse : snapshot.getChildren()) {
                    for (DataSnapshot dataSnapshot : userdataSnapuse.getChildren()) {
                        String name = dataSnapshot.child("caption").getValue(String.class);
                        String desc = dataSnapshot.child("text").getValue(String.class);
                        String price = "售價: " + dataSnapshot.child("itemprice").getValue(String.class);
                        String exchange = "希望交換物: " + dataSnapshot.child("itemchange").getValue(String.class);
                        String status = "可交換"; //寫死，之後有時間做出更新再來改
                        String image = dataSnapshot.child("imageURL").getValue(String.class);
                        String location = dataSnapshot.child("location").getValue(String.class);
                        String userImage = dataSnapshot.child("userImage").getValue(String.class);
                        String userName = dataSnapshot.child("userName").getValue(String.class);
                        String itemkey = dataSnapshot.getKey();
                        items.add(new itme_recycler(name, exchange, price, status, image, location,userName,userImage,itemkey));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(recycler_view.this, "資料讀取失敗", Toast.LENGTH_SHORT).show();
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(recycler_view.this, UploadItemActivity.class);
                startActivity(intent);
            }
        });

    }
}