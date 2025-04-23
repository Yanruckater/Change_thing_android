package com.example.change_things_android_final_demo;

import android.content.ClipData;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.change_things_android_final_demo.recyclerfile.MyAdapter;
import com.example.change_things_android_final_demo.recyclerfile.itme_recycler;

import java.util.ArrayList;
import java.util.List;

public class recycler_view extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recycler_view);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        List<itme_recycler> items = new ArrayList<>();
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱3", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱4", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱5", "希望交換物1", "$50.00", "可交換", R.drawable.ic_launcher_foreground));
        items.add(new itme_recycler("商品名稱1", "希望交換物1", "$10.00", "可交換", R.drawable.ic_launcher_foreground));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyAdapter(getApplicationContext(),items));


    }
}